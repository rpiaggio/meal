package com.rpiaggio.meal

import utest._

object KMPTest extends TestSuite {

  val tests = Tests {

    val parse = ParseUntil(ParseAction.Ignore, "ababababca")

    assert(parse.pi == Vector(0, 0, 1, 2, 3, 4, 5, 6, 0, 1))
  }

}
