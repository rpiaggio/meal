package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.Uri
import org.http4s.implicits._

object RedUTS extends FeedList {
  private val parsePattern = ParsePattern(
    """<article{*}
      |<a href="{%}"{*}
      |<div class="event-date entry-meta">{%}<header{*}
      |<div class="event-title">{%}</div>{*}
      |<div class="event-venue">{%}</div>{*}
      |<div class="event-btns entry-meta">{%}<a{*}
      |</div>{%}</a>{*}
      |</article>""".stripMargin
  )

  private val entryTemplate = EntryTemplate("{%3} - {%2} - {%5}", "{%1}", "{%6}<br/>{%4}")

  private val baseUri: Uri = uri"https://reduts.com.uy/categoria/?posts_per_page=100"

  private def buildFeed(title: String, category: String) = {
    val uri = baseUri / category
    Feed(Page.single(uri), FeedEntry(title, uri, title), parsePattern, entryTemplate)
  }

  val feeds = Map(
    "reduts-conciertos" -> buildFeed("RedUTS Conciertos", "conciertos"),
    "reduts-teatro" -> buildFeed("RedUTS Teatro", "teatro"),
    "reduts-electronica" -> buildFeed("RedUTS Electrónica", "electronica"),
    "reduts-especiales" -> buildFeed("RedUTS Especiales", "especiales")
  )
}
