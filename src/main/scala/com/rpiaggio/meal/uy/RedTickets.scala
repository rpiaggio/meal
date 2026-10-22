package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.Uri
import org.http4s.implicits._
import org.jsoup.Jsoup

// RedTickets is a GeneXus app. Category pages are server-rendered, 24 events per page, with a pager whose
// "next" link (/categoria/<id>/<name>/<page>, 0-based) disappears on the last page. The name in the path
// is ignored by the server, so ASCII names are used.
object RedTickets extends FeedList {
  private val parsePattern = ParsePattern(
    """<article class="rt-card-primary">{*}href="{%}"{*}rt-card__title"{*}>{%}</h3>{%}</article>"""
  )

  // Anchored on the mobile pager ("Página N de M"), where the next link is the first one after the
  // current page. On the last page it's a disabled span instead, so nothing matches.
  private val nextPattern = ParsePattern(
    """>Página {*}</span><span class="wPageSelector"{*}href="{%}""""
  )

  // Meta items are date and address, either of which can be missing.
  private def meta(data: EntryData): String =
    Jsoup
      .parseBodyFragment(data(2))
      .body
      .select("span.rt-meta")
      .eachText
      .toArray(Array.empty[String])
      .map(_.trim)
      .filter(_.nonEmpty)
      .mkString(" - ")

  private val entryTemplate = EntryTemplate("{%2}", "{%1}", meta(_))

  private val baseUri: Uri = uri"https://redtickets.uy/categoria"

  private def buildFeed(title: String, category: String, name: String) = {
    val uri = baseUri / category / name
    Feed(
      Page.single(uri),
      FeedEntry(title, uri, title),
      parsePattern,
      entryTemplate,
      followPages = Some(FollowPages(nextPattern))
    )
  }

  val feeds = Map(
    "redtickets-familiares" -> buildFeed("RedTickets Familiares", "1", "Familiares"),
    "redtickets-musica" -> buildFeed("RedTickets Música", "3", "Musica"),
    "redtickets-teatro" -> buildFeed("RedTickets Teatro", "6", "Teatro"),
    "redtickets-especiales" -> buildFeed("RedTickets Especiales", "7", "Especiales")
  )
}
