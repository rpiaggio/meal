package com.rpiaggio.meal

import cats.effect.ConcurrentEffect
import fs2.{Pull, Stream}

class FeedBuilder[F[_] : ConcurrentEffect] {
  private val client = new HttpClient[F]

  private def pageStream(feed: Feed, page: Option[Int] = None): Stream[F, FeedEntry] = {
    val url = feed.channelEntry.link

    client.stream(page.fold(url)(p => url.replace("$page", p.toString)))
      .through(feed.parser)
      .through(feed.formatter)
  }

  private def concatPages(feed: Feed)(pageSize: Int): Stream[F, FeedEntry] = {

    def processPage(page: Int): Pull[F, FeedEntry, Unit] = {

      def go(s: Stream[F, FeedEntry], remainingEntries: Int): Pull[F, FeedEntry, Unit] = {
        s.pull.uncons.flatMap {
          case Some((hd, tl)) => Pull.output(hd) >> go(tl, remainingEntries - hd.size)
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

  protected def feedStream(feed: Feed): Stream[F, FeedEntry] = {
    feed.pageSize.fold(pageStream(feed))(concatPages(feed))
  }
}
