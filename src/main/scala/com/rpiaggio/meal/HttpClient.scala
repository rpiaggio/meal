package com.rpiaggio.meal

import cats.effect.{Async, Resource}
import fs2.Stream
import org.http4s.{Headers, Request, Response, Uri}
import org.http4s.client.Client
import org.http4s.client.middleware.{CookieJar, FollowRedirect}
import org.http4s.blaze.client.BlazeClientBuilder

class HttpClient[F[_]](
    base: Resource[F, Client[F]]
)(implicit F: Async[F]):
  def this()(implicit F: Async[F]) = this(BlazeClientBuilder[F].resource)

  // Each session gets its own cookie jar, so stateful sites (eg: Wicket) see follow-up requests
  // as part of the same session. Redirects go through the jar too, since sessions are often set mid-redirect.
  val session: Resource[F, Client[F]] =
    base
      .evalMap(CookieJar.impl[F])
      .map(FollowRedirect(MaxRedirCount, _ => false))

  def checkStatus(uri: Uri): fs2.Pipe[F, Response[F], Response[F]] = _.flatMap { res =>
    if res.status.isSuccess then Stream.emit(res)
    else Stream.raiseError[F](new Exception(s"Failed to fetch [$uri] - Status: [${res.status}]"))
  }

  def fetch(client: Client[F], uri: Uri, headers: Headers = Headers.empty): Stream[F, String] =
    client
      .stream(Request[F](uri = uri, headers = headers))
      .through(checkStatus(uri))
      .flatMap(_.body.chunks.through(fs2.text.utf8.decodeC))

  def stream(uri: Uri): Stream[F, String] =
    Stream.resource(session).flatMap(fetch(_, uri))
