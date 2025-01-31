package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.Uri
import org.http4s.implicits._

object RedTickets extends FeedList {
  private val parsePattern = ParsePattern(
    """"card-inner"{*}
      |href="{%}"{*}
      |EventTitle{*}>{%}<{*}
      |EventInfo{*}>{%}<{*}
      |EventInfo{*}>{%}<""".stripMargin
  )

  private val entryTemplate = EntryTemplate("{%2}", "{%1}", "{%3} - {%4}")

  private val baseUri: Uri = uri"https://redtickets.uy/busqueda"

  private def buildFeed(title: String, category: String) = {
    val uri = baseUri.withPath(baseUri.path.toString + s"?*,$category,0")
    Feed(
      Page.single(uri),
      FeedEntry(title, uri, title),
      parsePattern,
      entryTemplate
    )
  }

  val feeds = Map(
    "redtickets-familiares" -> buildFeed("RedTickets Familiares", "1"),
    "redtickets-musica" -> buildFeed("RedTickets Música", "3"),
    "redtickets-teatro" -> buildFeed("RedTickets Teatro", "6"),
    "redtickets-especiales" -> buildFeed("RedTickets Especiales", "7")
  )
}
