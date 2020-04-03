package com.rpiaggio.meal.uy

import com.rpiaggio.meal._

object RedTickets extends FeedList {
  private val parsePattern = ParsePattern(
    """"card-inner"{*}
      |href="{%}"{*}
      |EventTitle{*}>{%}<{*}
      |EventInfo{*}>{%}<{*}
      |EventInfo{*}>{%}<""".stripMargin
  )

    // NO ESTARIA FUNCIONANDO...

  private val entryTemplate = FeedEntry("{%2}", "{%1}", "{%3} - {%4}")

  private def buildFeed(title: String, url: String) =
    Feed(FeedEntry(title, url, title), parsePattern, entryTemplate)

  val feeds = Map(
    "redtickets-familiares" -> buildFeed("RedTickets Familiares", "https://redtickets.uy/busqueda?*,1,0"),
    "redtickets-musica" -> buildFeed("RedTickets Música", "https://redtickets.uy/busqueda?*,3,0"),
    "redtickets-teatro" -> buildFeed("RedTickets Teatro", "https://redtickets.uy/busqueda?*,6,0"),
    "redtickets-especiales" -> buildFeed("RedTickets Especiales", "https://redtickets.uy/busqueda?*,7,0")
  )
}
