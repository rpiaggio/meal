package com.rpiaggio.meal

import cats.effect._
import cats.implicits._
import io.github.howardjohn.lambda.http4s.Http4sLambdaHandler

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val ce: ConcurrentEffect[IO] = IO.ioConcurrentEffect

  private val host = "localhost"
  private val port = 8080

  private val server = new WebServer(AllFeeds.feeds, host, port)

  override def run(args: List[String]): IO[ExitCode] = {

    println(s"Server online at http://$host:$port/")
    println("Valid feeds:")
    AllFeeds.feeds.keys.toSeq.sorted.foreach(key => println(s"* $key"))

    server.build.as(ExitCode.Success)
  }

  class EntryPoint extends Http4sLambdaHandler(server.service)
}
