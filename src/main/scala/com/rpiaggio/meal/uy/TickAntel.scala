package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.Uri
import org.http4s.implicits._
import cats.implicits._

object TickAntel extends FeedList {
  private val parsePattern = ParsePattern(
    """"nombre":"{%}"{*}
      |"fechaProxFuncion":{%},{*}
      |"salaProxFuncion":"{%}"{*}
      |"lugarProxFuncion":"{%}"{*}
      |"urlMovil":"{%}"
      |""".stripMargin
  )

  private val ItemsPerPage: Int = 20

  /// TODO Resolver fecha y link de evento (y de categoria?)

  private val entryTemplate = EntryTemplate("{%1} - {%2}", "{%5}", "{%4} - {%3}")

  private val baseUri: Uri = uri"https://tickantel.com.uy/tickantelmovil/v3/productos"

  private def buildFeed(title: String, category: String) = {
    val uri = baseUri.withQueryParam("categorias", category)
    Feed(
      Page.asQueryParamsRange(uri, ItemsPerPage, "inicio"),
      FeedEntry(title, uri, title),
      parsePattern,
      entryTemplate,
      ItemsPerPage.some
    )
  }

  val feeds = Map(
    "tickantel-musica" -> buildFeed("TickAntel Música", "Música"),
    "tickantel-teatro" -> buildFeed("TickAntel Teatro", "Teatro")
  )
}
