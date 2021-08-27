package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.Uri
import org.http4s.implicits._

object DSM extends FeedList {
  private val parsePattern = ParsePattern(
    """<article{*}
      |<h2{*}
      |<a{*}href="{%}"{*}>{%}</a>{*}
      |</h2>{%}</div>{*}
      |</article>""".stripMargin
  )

  private val entryTemplate = EntryTemplate("{%2}", "{%1}", "{%3}")

  private def buildFeed(title: String, uri: Uri) =
    Feed(
      Page.single(uri),
      FeedEntry(title, uri, title),
      parsePattern,
      entryTemplate
    )

  val feeds = Map(
    "dsm-noticias" -> buildFeed(
      "DSM - Noticias",
      uri"https://www.dsm.edu.uy/es/dsm/noticias.html"
    )
  )
}
