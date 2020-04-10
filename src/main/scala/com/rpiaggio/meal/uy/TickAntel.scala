package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.Uri
import org.http4s.implicits._
import cats.implicits._
import java.time.format.DateTimeFormatter
import java.{util => ju}
import java.time.Instant
import java.time.ZoneId
import java.net.URLEncoder

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

  /// TODO Resolver link de evento (y de categoria?)

  private val dtf = DateTimeFormatter.ofPattern(
    "dd 'de' MMMM (eeee)",
    ju.Locale.forLanguageTag("es")
  )

  private val zone = ZoneId.of("America/Montevideo")

  private val entryTemplate =
    EntryTemplate(
      "{%1} - {%2}"
        .mapAt(2, s => dtf.format(Instant.ofEpochMilli(s.toLong).atZone(zone))),
      "{%5}".mapAt(
        5,
        _.split("/").map(s => URLEncoder.encode(s, "UTF-8")).mkString("/")
      ),
      "{%4} - {%3}"
    )

  private val baseUri: Uri =
    uri"https://tickantel.com.uy/tickantelmovil/v3/productos"

  private val entryBaseUri: Uri =
    uri"https://tickantel.com.uy/inicio/buscar_categoria?categoria=M%C3%BAsica&cat_id=1"

  private def buildFeed(title: String, category: String, categoryId: String) = {
    val uri = baseUri.withQueryParam("categorias", category)
    Feed(
      Page.asQueryParamsRange(uri, ItemsPerPage, "inicio"),
      FeedEntry(
        title,
        entryBaseUri
          .withQueryParams(
            Map("categoria" -> category, "cat_id" -> categoryId)
          ),
        title
      ),
      parsePattern,
      entryTemplate,
      ItemsPerPage.some
    )
  }

  val feeds = Map(
    "tickantel-musica" -> buildFeed("TickAntel Música", "Música", "1"),
    "tickantel-teatro" -> buildFeed("TickAntel Teatro", "Teatro", "2")
  )
}
