package com.rpiaggio.meal

import cats.effect.{ConcurrentEffect, Timer}
import fs2._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import org.http4s.headers.`Content-Type`

class WebServer[F[_] : ConcurrentEffect : Timer](feeds: Map[String, Feed], host: String, port: Int) extends Http4sDsl[F] {
  val client = new HttpClient[F]

  // TODO: MULTI PAGE FEEDS!!!!

  def render(feed: Feed): Stream[F, Chunk[Byte]] = RssRenderer(feed.channelEntry,
    client.stream(feed.channelEntry.link)
      .through(feed.parser)
      .through(feed.formatter)
      .handleErrorWith { e =>
        e.printStackTrace()
        Stream.emit(FeedEntry(e.getMessage, "", e.getStackTrace.map(_.toString).mkString("\n")))
      }
  )

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / feedName =>
      feeds.get(feedName).fold(NotFound("Invalid feed name."))(feed =>
        Ok(render(feed), `Content-Type`(MediaType.application.`rss+xml`, Charset.`UTF-8`))
      )
  }

  def build: F[Unit] =
    BlazeServerBuilder[F]
      .bindHttp(port, host)
      .withHttpApp(Router("/" -> service).orNotFound)
      .serve
      .compile
      .drain
  //      .as(ExitCode.Success)
}
