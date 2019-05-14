package com.rpiaggio.meal

import cats.effect.{ConcurrentEffect, ContextShift, Effect, ExitCode, IO, IOApp, Timer}
import com.github.gvolpe.http4s.StreamUtils
import org.http4s.client.middleware.{FollowRedirect, FollowRedirectWithCookies, RequestLogger, ResponseLogger}
import org.http4s.{EntityDecoder, Request, Status, Uri}
import fs2.Stream

//import scala.concurrent.ExecutionContext.global
import org.http4s.client.blaze._
import org.http4s.client._

import scala.concurrent.ExecutionContext.Implicits.global

class HttpClient[F[_]](implicit F: ConcurrentEffect[F]) {
  private val client = BlazeClientBuilder(global)

  def stream(uri: String): Stream[F, String] = {
    val request = Request[F](uri = Uri.fromString(uri).right.get)
    for {
      client <- client.stream
      res <- FollowRedirectWithCookies(MAX_REDIR_COUNT)(client).stream(request).flatMap(_.body.chunks.through(fs2.text.utf8DecodeC))
    } yield res
  }
}

object Test extends App {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  val feed = uy.TickAntel.feeds.head._2

  val client = new HttpClient[IO]
  val utils = implicitly[StreamUtils[IO]]

  val out =
    for {
      response <- client.stream(feed.channelEntry.link).through(feed.parser).through(feed.formatter)
      _ <- utils.putStrLn(response.toString)
    } yield ()
  out.compile.drain.unsafeRunSync()
}
