package com.rpiaggio.meal

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.Stream
import fs2.concurrent.SignallingRef
import org.http4s.{Charset, HttpRoutes, MediaType}
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import org.http4s.headers.`Content-Type`

import scala.concurrent.ExecutionContext.Implicits.global


object Test extends IOApp {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit override val timer: Timer[IO] = IO.timer(global)

  val feed = uy.TickAntel.feeds.head._2

  val client = new HttpClient[IO]

  val outputStream = RssRenderer(feed.channelEntry, client.stream(feed.channelEntry.link).through(feed.parser).through(feed.formatter))

  val service = HttpRoutes.of[IO] {
    case _ => Ok(outputStream, `Content-Type`(MediaType.application.`rss+xml`, Charset.`UTF-8`))
  }

  override def run(args: List[String]) = {
//    val server =
      BlazeServerBuilder[IO]
        .bindHttp(8080, "localhost")
        .withHttpApp(Router("/" -> service).orNotFound)
//
//    val httpService =
//      for {
//        signal <- Stream.eval(SignallingRef[IO, Boolean]{System.in.read(); true})
//        exitCode <- Stream.eval(Ref[IO].of(ExitCode.Success))
//        serve <- server.serveWhile(signal, exitCode)
//      } yield serve
//
//    httpService
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
