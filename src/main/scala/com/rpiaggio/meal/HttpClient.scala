package com.rpiaggio.meal

import cats.effect.ConcurrentEffect
import fs2.Stream
import org.http4s.{Request, Uri}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.FollowRedirect

import scala.concurrent.ExecutionContext.Implicits.global

class HttpClient[F[_]](implicit F: ConcurrentEffect[F]) {
  private val client = BlazeClientBuilder(global)

  def stream(uri: String): Stream[F, String] = {
    val request = Request[F](uri = Uri.fromString(uri).getOrElse(throw new Exception(s"Invalid URI [$uri].")))
    for {
      client <- client.stream
      res <- FollowRedirect(MAX_REDIR_COUNT, _ => false)(client).stream(request).flatMap(_.body.chunks.through(fs2.text.utf8DecodeC))
    } yield res
  }
}