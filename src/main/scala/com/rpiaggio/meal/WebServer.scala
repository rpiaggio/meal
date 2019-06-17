package com.rpiaggio.meal

import cats.effect._
import cats.implicits._
import fs2.Stream
import org.http4s.{Charset, HttpRoutes, MediaType}
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import org.http4s.headers.`Content-Type`
import io.github.howardjohn.lambda.http4s.Http4sLambdaHandler

import scala.concurrent.ExecutionContext.Implicits.global


object WebServer extends IOApp {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit override val timer: Timer[IO] = IO.timer(global)

  val client = new HttpClient[IO]

  def render(feed: Feed) = RssRenderer(feed.channelEntry,
    client.stream(feed.channelEntry.link)
      .through(feed.parser)
      .through(feed.formatter)
      .handleErrorWith { e =>
        e.printStackTrace()
        Stream.emit(FeedEntry(e.getMessage, "", e.getStackTrace.map(_.toString).mkString("\n")))
      }
  )

  val service = HttpRoutes.of[IO] {
    case GET -> Root / feedName =>
      AllFeeds.feeds.get(feedName).fold(NotFound("Invalid feed name."))(feed =>
        Ok(render(feed), `Content-Type`(MediaType.application.`rss+xml`, Charset.`UTF-8`))
      )
  }

  override def run(args: List[String]) = {
    val host = "localhost"
    val port = 8080

    println(s"Server online at http://$host:$port/")
    println("Valid feeds:")
    AllFeeds.feeds.keys.toSeq.sorted.foreach(key => println(s"* $key"))

    //    val server =
    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withHttpApp(Router("/" -> service).orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

  class EntryPoint extends Http4sLambdaHandler(service)
}
