package com.rpiaggio.meal

import cats.effect.IO
import utest._

object EntityParserTest extends TestSuite {
  import cats._
  import cats.implicits._
  import fs2._
  //  import scala.concurrent.duration._


  val pattern = ParsePattern(List(ParseUntil(ParseAction.Ignore, ">>>"), ParseUntil(ParseAction.Capture, "<<<")))
  val parser = EntityParser[Id](pattern)

  val stream = Stream.emits[Id, String](List("1", "x2f", "e z", ">>", ">HO", "LA!", "<", "<<", "je", ">", ">", ">", ">", ">", ":", ")", "<", "<", "OSO<<<")) //.metered(1.seconds)

  val converted =
    stream
//      .through(_.map { x => println(s"< $x"); x })
      .through(parser)
//      .through(_.map { x => println(s">> $x"); x })
      .compile
      .toList

  val tests = Tests {
    Symbol("streamConvert") - {
      assert(converted == List(List("HOLA!"), List(">>:)<<OSO")))
    }
  }
}
