package com.rpiaggio.meal

import cats.effect._
import cats.implicits._
import feral.lambda.{ApiGatewayProxyInvocation, IOLambda}
import feral.lambda.events.{ApiGatewayProxyEvent, ApiGatewayProxyResult}
import feral.lambda.http4s.ApiGatewayProxyHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext

object Main extends IOApp {
  private val host = "localhost"
  private val port = 8081

  private val server = new WebServer[IO](AllFeeds.feeds, host, port)

  override def run(args: List[String]): IO[ExitCode] = {
    println(s"Server online at http://$host:$port/")
    println("Valid feeds:")
    AllFeeds.feeds.keys.toSeq.sorted.foreach(key => println(s"* $key"))

    server.build.as(ExitCode.Success)
  }

  // serverless.yml uses REST API (`http`) events, which send v1 proxy payloads.
  class EntryPoint extends IOLambda[ApiGatewayProxyEvent, ApiGatewayProxyResult] {
    def handler =
      Resource.pure[IO, ApiGatewayProxyInvocation[IO] => IO[Option[ApiGatewayProxyResult]]] {
        implicit inv => ApiGatewayProxyHandler(server.service.orNotFound)
      }
  }
}
