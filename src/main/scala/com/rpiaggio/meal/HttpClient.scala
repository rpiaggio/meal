package com.rpiaggio.meal

import cats.effect.ConcurrentEffect
import fs2.Stream
import org.http4s.{Request, Uri}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.FollowRedirect

import scala.concurrent.ExecutionContext.Implicits.global

class HttpClient[F[_]](implicit F: ConcurrentEffect[F]) {
  private val client = BlazeClientBuilder(global)

  def stream(uri: Uri): Stream[F, String] = {
    // println(s"FETCHING [$uri]")

    val request = Request[F](uri = uri)
    for {
      client <- client.stream
      res <- FollowRedirect(MaxRedirCount, _ => false)(client)
        .stream(request)
        .flatMap(_.body.chunks.through(fs2.text.utf8DecodeC))
    } yield res
  }
}
