package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.implicits._
import utest._

object RedTicketsTest extends TestSuite {
  private val feed = RedTickets.feeds("redtickets-musica")
  private val follow = feed.followPages.get

  val tests = Tests {
    test("firstPage") {
      val body = Fixtures("redtickets-first.html")
      val entries = feed.entries(body)
      assert(entries.size == 24)
      assert(entries.map(_.uri).distinct.size == 24)
      assert(
        entries.head == FeedEntry(
          "Gabinete del Rock",
          uri"https://redtickets.uy/evento/Gabinete-del-Rock/34295/",
          "Viernes 16 de Octubre - Bartolomé Mitre 1323"
        )
      )
      assert(entries.forall(e => e.title.nonEmpty && e.description.nonEmpty))
      assert(
        follow.nextUri(body, uri"https://redtickets.uy/categoria/3/Musica") ==
          Some(uri"https://redtickets.uy/categoria/3/Musica/1")
      )
    }

    test("lastPage") {
      val body = Fixtures("redtickets-last.html")
      assert(feed.entries(body).size == 23)
      assert(follow.nextUri(body, uri"https://redtickets.uy/categoria/3/Musica/11").isEmpty)
    }
  }
}
