package com.rpiaggio.meal.google

import com.rpiaggio.meal._

object Calendar extends FeedList {
  private val parsePattern = ParsePattern(
    """<div class=\"box\">{*}
      |<div class=\"box-title\"><a href=\'{%}\'>{%}</a>{*}
      |</div></div>""".stripMargin
  )

  private val entryTemplate = FeedEntry("{%2}", "{%1}", "")

  private def buildFeed(title: String, url: String) =
    Feed(FeedEntry(title, url, title), parsePattern, entryTemplate)

  // TODO: MULTIPAGE!

  val feeds = Map(
    "calendar-rpiaggio" -> buildFeed("My Calendar", "https://calendar.google.com/calendar/ical/rpiaggio%40gmail.com/private-0980ea4c2b25f1a2f780ce32c98a1484/basic.ics"),
  )
}
