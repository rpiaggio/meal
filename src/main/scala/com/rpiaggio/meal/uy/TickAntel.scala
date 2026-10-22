package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.{Header, Headers, Uri}
import org.http4s.implicits._
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.typelevel.ci._

// TickAntel is an Apache Wicket app. The category page only renders the first results; the rest come from
// a "load more" Ajax behavior whose URL is embedded in the page (and in every Ajax response, until there's
// nothing left). Those URLs are bound to the JSESSIONID session and to the page version, and each response
// re-renders the whole list so far.
object TickAntel extends FeedList {
  // Cards vary (one or two dates, optional subtitle, sometimes no date at all), so the whole card content
  // is captured and picked apart with Jsoup.
  private val parsePattern = ParsePattern(
    """<div class="item">{*}href="{%}"{*}<div class="card-content">{%}</div>"""
  )

  private val nextPattern = ParsePattern(
    """cargarMasResultados=function (){*}"u":"{%}""""
  )

  // Wicket answers 400 to Ajax behaviors without these. Any base URL is accepted.
  private val ajaxHeaders = Headers(
    Header.Raw(ci"Wicket-Ajax", "true"),
    Header.Raw(ci"Wicket-Ajax-BaseURL", ".")
  )

  private def cardText(data: EntryData, selector: String, separator: String): String = {
    val card = Jsoup.parseBodyFragment(data(1)).body
    card
      .select(selector)
      .eachText
      .toArray(Array.empty[String])
      .map(_.replace('\u00a0', ' ').trim)
      .filter(_.nonEmpty)
      .distinct // Venue and room often repeat (eg: "Antel Arena - Antel Arena").
      .mkString(separator)
  }

  private val entryTemplate =
    EntryTemplate(
      data => {
        val title = cardText(data, "p.title span", " - ")
        val date = cardText(data, "p.auto-pf-date", " ")
        if (date.isEmpty) title else s"$title ($date)"
      },
      "{%1}",
      cardText(_, "p:not([class]) span", " - ")
    )

  private val baseUri: Uri = uri"https://tickantel.com.uy/inicio/buscar_categoria"

  private def buildFeed(title: String, categoryId: String): Feed = {
    val uri = baseUri.withQueryParam("cat_id", categoryId)

    Feed(
      Page.single(uri),
      FeedEntry(title, uri, title),
      parsePattern,
      entryTemplate,
      followPages = Some(FollowPages(nextPattern, ajaxHeaders))
    )
  }

  val feeds = Map(
    "tickantel-musica" -> buildFeed("TickAntel Música", "2"),
    "tickantel-teatro" -> buildFeed("TickAntel Teatro", "1")
  )
}
