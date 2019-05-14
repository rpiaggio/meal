package com.rpiaggio.meal

import cats.effect.ConcurrentEffect
import fs2.Stream
import org.http4s.{Request, Uri}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.FollowRedirectWithCookies

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