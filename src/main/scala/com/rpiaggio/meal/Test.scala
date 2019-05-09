package com.rpiaggio.meal

import cats.effect.{ContextShift, IO, Timer}
import org.http4s.client.middleware.{FollowRedirect, FollowRedirectWithCookies, RequestLogger, ResponseLogger}
import org.http4s.{EntityDecoder, Status}

//import scala.concurrent.ExecutionContext.global
import org.http4s.client.blaze._
import org.http4s.client._

import scala.concurrent.ExecutionContext.Implicits.global

object Test extends App {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  val url = "https://tickantel.com.uy/inicio/buscar_categoria?0&cat_id=1"
//  val url = "https://reduts.com.uy/categoria/conciertos/?posts_per_page=100"

  def get() = {
    BlazeClientBuilder[IO](global).resource.use { client =>
//      val httpClient = ResponseLogger(true, true, logAction = Some{s:String => IO(println(s))})(client)
      val httpClient = FollowRedirectWithCookies(5)(client)

      httpClient.get(url) {
        case Status.Successful(r) => r.attemptAs[String].leftMap(_.message).value
        case r => r.as[String].map(b => Left(s"Request failed with status ${r.status.code} and body $b"))
      }

    }
  }

  println(get().unsafeRunSync())
}
