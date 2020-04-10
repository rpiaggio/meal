package com.rpiaggio.meal.google

import com.rpiaggio.meal._
import org.http4s.Uri
import org.http4s.implicits._

object Calendar extends FeedList {
  private val parsePattern = ParsePattern(
    """<div class=\"box\">{*}
      |<div class=\"box-title\"><a href=\'{%}\'>{%}</a>{*}
      |</div></div>""".stripMargin
  )

  private val entryTemplate = EntryTemplate("{%2}", "{%1}", "")

  private def buildFeed(title: String, uri: Uri) =
    Feed(
      Page.single(uri),
      FeedEntry(title, uri, title),
      parsePattern,
      entryTemplate
    )

  // TODO: MULTIPAGE!

  val feeds = Map(
    "calendar-rpiaggio" -> buildFeed(
      "My Calendar",
      uri"https://calendar.google.com/calendar/ical/rpiaggio%40gmail.com/private-0980ea4c2b25f1a2f780ce32c98a1484/basic.ics"
    )
  )
}
