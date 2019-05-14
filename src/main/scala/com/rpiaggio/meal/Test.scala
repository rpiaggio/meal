package com.rpiaggio.meal

import cats.effect.{ContextShift, ExitCode, IO, IOApp, Timer}
import com.github.gvolpe.http4s.StreamUtils

import scala.concurrent.ExecutionContext.Implicits.global



object Test extends IOApp {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit override val timer: Timer[IO] = IO.timer(global)

  val feed = uy.TickAntel.feeds.head._2

  val client = new HttpClient[IO]

  val outputStream = RssRenderer(feed.channelEntry, client.stream(feed.channelEntry.link).through(feed.parser).through(feed.formatter))

  val utils = implicitly[StreamUtils[IO]]

  val out =
    for {
      response <- outputStream
      _ <- utils.putStr(response.toString)
    } yield ()

  override def run(args: List[String]) = out.compile.drain.map(_ => ExitCode.Success)
}
