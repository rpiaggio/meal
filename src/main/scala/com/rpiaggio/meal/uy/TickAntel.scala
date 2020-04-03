package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.Uri
import org.http4s.implicits._

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

  private val entryTemplate = EntryTemplate("{%3} - {%2}", "{%1}", "{%4}")

  private val baseUri: Uri = uri"https://tickantel.com.uy/inicio/buscar_categoria"

  private def buildFeed(title: String, category: String) = {
    val uri = baseUri.withQueryParam("cat_id", category)
    Feed(Page.single(uri), FeedEntry(title, uri, title), parsePattern, entryTemplate)
  }

  val feeds = Map(
    "tickantel-musica" -> buildFeed("TickAntel Música", "1"),
    "tickantel-teatro" -> buildFeed("TickAntel Teatro", "2")
  )
}
