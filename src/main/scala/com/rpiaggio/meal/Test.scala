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
      res <- FollowRedirectWithCookies(5)(client).stream(request).flatMap(_.body.chunks.through(fs2.text.utf8DecodeC))
    } yield res
  }
}


object Test extends App {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  val url = "https://tickantel.com.uy/inicio/buscar_categoria?0&cat_id=1"
  //  val url = "https://reduts.com.uy/categoria/conciertos/?posts_per_page=100"

  val client = new HttpClient[IO]
  val utils = implicitly[StreamUtils[IO]]

  val out =
    for {
      response <- client.stream(url)
      _ <- utils.putStr(response)
    } yield ()
  out.compile.drain.unsafeRunSync()
}
