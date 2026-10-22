package com.rpiaggio.meal

import scala.io.Source

// Pages saved from the real sites. Refresh them when a site changes and the live tests start failing.
object Fixtures {
  def apply(name: String): String = {
    val source = Source.fromResource(s"fixtures/$name", getClass.getClassLoader)(using scala.io.Codec.UTF8)
    try source.mkString
    finally source.close()
  }
}
