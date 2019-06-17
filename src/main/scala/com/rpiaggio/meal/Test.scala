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

  /*override*/ def run1(args: List[String]) = {
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

  import fs2._
  import atto._
  import Atto._
  import atto.ParseResult._

  def parseBetween[F[_], A](p: Parser[A]): Pipe[F, String, String] = s => {

    def exhaust(r: ParseResult[A], acc: List[String]): (ParseResult[A], List[String]) =
      r match {
        case Done(in, a)    => exhaust(p.parse(in), in :: acc)
        case Fail(in, _, _) => exhaust(p.parse(in.drop(1)), acc)
        case Partial(_)     => (r, acc)
      }

    def go(r: ParseResult[A])(s: Stream[F, String]): Pull[F, String, Unit] = {
      s.pull.uncons1.flatMap{
        case Some((s, rest)) =>
          val (r0, acc) = r match {
            case Done(in, a)    => (p.parse(in + s), List(in))
            case Fail(in, _, _) => (p.parse(in.drop(1) + s), Nil)
            case Partial(_)     => (r.feed(s), Nil)
          }
          val (r1, as) = exhaust(r0, acc)
          Pull.output(Chunk.seq(as.reverse)) >> go(r1)(rest)
        case None => Pull.output(Chunk.seq(exhaust(r.done, Nil)._2))
      }
    }
    go(p.parse(""))(s).stream
  }

  override def run(args: List[String]) = {
    import cats._
    import cats.implicits._
    import fs2._
    import atto._
    import Atto._
    import atto.fs2.Pipes
    import scala.concurrent.duration._

    //    val p = many(digit)

    val stream = Stream.emits[IO, String](List("1", "x2f", "e z", "3", "f", "g", "h", "y"))//.metered(1.seconds)
    //
    //    val stream = Stream.emit[IO, String]("12e3f")

//    val p = for { n <- int; c <- take(n) } yield c

    val p = /*string("x2") <~ many(anyChar) <~*/ string("x")

//    val p = bracket(string("x2"), takeWhile(_ => true), string("3"))

    val xxx =
      stream
        .through(_.map { x => println(s">> $x"); x })
        //          .delayBy(
        .through(parseBetween(p))
        .through(_.map { x => println(s"<< $x"); x })
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
}
