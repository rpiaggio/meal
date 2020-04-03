package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.Uri
import org.http4s.implicits._

object Abitab extends FeedList {
  private val parsePattern = ParsePattern(
    """<div class=\"box\">{*}
      |<div class=\"box-title\"><a href=\'{%}\'>{%}</a>{*}
      |</div></div>""".stripMargin
  )

  private val entryTemplate = EntryTemplate("{%2}", "{%1}", "")

  private val linkToUri: Uri =
    uri"https://www.abitab.com.uy/innovaportal/v/11710/14/abitab/eventos.html"

  private val baseUri: Uri =
    uri"https://www.abitab.com.uy/abitab/AjaxPromo?idroot=1&site=12&channel=abitab"

  private def buildFeed(title: String, category: String) = {
    val categoryUri = baseUri.withQueryParam("categoria", category)
    Feed(
      Page.asQueryParam(categoryUri),
      FeedEntry(title, linkToUri, title),
      parsePattern,
      entryTemplate,
      Some(12)
    )
  }

  val feeds = Map(
    "abitab-musica" -> buildFeed("Abitab Música", "12071"),
    "abitab-danza" -> buildFeed("Abitab Danza", "12899"),
    "abitab-teatro" -> buildFeed("Abitab Teatro", "12072"),
    "abitab-varios" -> buildFeed("Abitab Varios", "11785")
  )
}
