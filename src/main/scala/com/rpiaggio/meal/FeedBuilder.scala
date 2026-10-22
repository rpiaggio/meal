package com.rpiaggio.meal

import cats.implicits._
import cats.effect.Async
import fs2.{Chunk, Pull, Stream}
import org.http4s.{Headers, Uri}
import org.http4s.client.Client

class FeedBuilder[F[_]: Async](client: HttpClient[F]) {

  private def pageStream(
      feed: Feed,
      page: Option[Page] = None
  ): Stream[F, FeedEntry] = {
    client
      .stream(feed.uriBuilder(page.orEmpty))
      .debug()
      .through(feed.parser) //.debug()
      .through(feed.formatter) //.debug()
  }

  private def concatPages(feed: Feed)(pageSize: Int): Stream[F, FeedEntry] = {

    def processPage(page: Page): Pull[F, FeedEntry, Unit] = {

      def go(
          s: Stream[F, FeedEntry],
          remainingEntries: Int
      ): Pull[F, FeedEntry, Unit] = {
        s.pull.uncons.flatMap {
          case Some((hd, tl)) =>
            Pull.output(hd) >> go(tl, remainingEntries - hd.size)
          case None =>
            if (remainingEntries <= 0)
              processPage(page + 1)
            else
              Pull.done
        }
      }

      go(pageStream(feed, Some(page)), pageSize)
    }

    processPage(1).stream
  }

  private def followPages(feed: Feed)(follow: FollowPages): Stream[F, FeedEntry] = {
    def go(
        http: Client[F],
        uri: Uri,
        headers: Headers,
        seen: Set[Uri],
        pagesLeft: Int
    ): Pull[F, FeedEntry, Unit] =
      Pull.eval(client.fetch(http, uri, headers).compile.string).flatMap { body =>
        val fresh = feed.entries(body).filterNot(e => seen(e.uri)).distinctBy(_.uri)
        Pull.output(Chunk.from(fresh)) >>
          (follow.nextUri(body, uri) match {
            case Some(next) if fresh.nonEmpty && pagesLeft > 1 =>
              go(http, next, follow.headers, seen ++ fresh.map(_.uri), pagesLeft - 1)
            case _ => Pull.done
          })
      }

    Stream
      .resource(client.session)
      .flatMap(http => go(http, feed.uriBuilder(1), Headers.empty, Set.empty, MaxFollowedPages).stream)
  }

  def feedStream(feed: Feed): Stream[F, FeedEntry] =
    feed.followPages
      .map(followPages(feed))
      .getOrElse(feed.pageSize.fold(pageStream(feed))(concatPages(feed)))
}
