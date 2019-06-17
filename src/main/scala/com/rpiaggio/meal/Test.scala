package com.rpiaggio.meal

import cats.effect._
import cats.implicits._
import org.http4s.{Charset, HttpRoutes, MediaType}
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import org.http4s.headers.`Content-Type`

import scala.annotation.tailrec
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

  def run2(args: List[String]) = {
    import cats._
    import cats.implicits._
    import fs2._
    import scala.concurrent.duration._

    //    val p = many(digit)

    val stream = Stream.emits[IO, String](List("1", "x2f", "e z", "3", "f", "g", "h", "y")).metered(1.seconds)
    //
    //    val stream = Stream.emit[IO, String]("12e3f")

    //    val p = for { n <- int; c <- take(n) } yield c

    //    val p = /*string("x2") <~ many(anyChar) <~*/ string("x")

    //    val p = bracket(string("x2"), takeWhile(_ => true), string("3"))

    val xxx =
      stream
        .through(_.map { x => println(s"< $x"); x })
        //          .delayBy(
        //        .through(parseBetween(p))
        .through(_.map { x => println(s">> $x"); x })
        .compile
        .toList
    //        .toRight("Nothing Emited")
    //        .flatMap(_.either)

    xxx.map { r =>
      println(r)
      ExitCode.Success
    }

    //    ???
  }

  def run3(args: List[String]) = {
    IO {
      //      val p = "ababababca"
      //      println(computeKMPPi(p))

      ExitCode.Success
    }
  }
}
