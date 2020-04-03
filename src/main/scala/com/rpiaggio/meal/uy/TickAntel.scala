package com.rpiaggio.meal.uy

import com.rpiaggio.meal._

object TickAntel extends FeedList {
  private val parsePattern = ParsePattern(
    """<div class="item">{*}
      |href="{%}"{*}
      |<p class="txt-upper{*}>{%}</p>{*}
      |<span class="span-block">{%}</span>{*}
      |<p>{%}</p>{*}
      |</div>""".stripMargin
  )

    // AGREGAR PAGINACION!

  private val entryTemplate = FeedEntry("{%3} - {%2}", "{%1}", "{%4}")

  private def buildFeed(title: String, url: String) =
    Feed(FeedEntry(title, url, title), parsePattern, entryTemplate)

  val feeds = Map(
    "tickantel-musica" -> buildFeed("TickAntel Música", "https://tickantel.com.uy/inicio/buscar_categoria?cat_id=1"),
    "tickantel-teatro" -> buildFeed("TickAntel Teatro", "https://tickantel.com.uy/inicio/buscar_categoria?cat_id=2")
  )
}
