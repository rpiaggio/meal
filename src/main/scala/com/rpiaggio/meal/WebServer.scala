package com.rpiaggio.meal

import cats.effect.{ConcurrentEffect, Timer}
import fs2._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import org.http4s.headers.`Content-Type`
import scala.concurrent.ExecutionContext

class WebServer[F[_]: ConcurrentEffect: Timer](
    feeds: Map[String, Feed],
    host: String,
    port: Int
) extends FeedBuilder[F]
    with Http4sDsl[F] {

  private def render(feed: Feed): Stream[F, Chunk[Byte]] =
    RssRenderer(
      feed.channelEntry,
      feedStream(feed)
        .handleErrorWith { e =>
          e.printStackTrace()
          Stream.emit(
            FeedEntry(
              e.getMessage,
              uri"https://google.com",
              e.getStackTrace.map(_.toString).mkString("\n")
            )
          )
        }
    )

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / feedName =>
      feeds
        .get(feedName)
        .fold(NotFound("Invalid feed name."))(feed =>
          Ok(
            render(feed),
            `Content-Type`(MediaType.application.`rss+xml`, Charset.`UTF-8`)
          )
        )
  }

  def build(ec: ExecutionContext): F[Unit] =
    BlazeServerBuilder[F](ec)
      .bindHttp(port, host)
      .withHttpApp(Router("/" -> service).orNotFound)
      .serve
      .compile
      .drain
}
