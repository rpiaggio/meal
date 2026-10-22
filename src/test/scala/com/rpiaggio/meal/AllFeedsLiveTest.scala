package com.rpiaggio.meal

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import utest._

// Hits the real sites, to find out whether any of them changed. Excluded from `sbt test`; run with `sbt Live/test`.
object AllFeedsLiveTest extends TestSuite {
  private val builder = new FeedBuilder[IO](new HttpClient[IO])

  private def problems(name: String, feed: Feed): IO[List[String]] =
    builder.feedStream(feed).compile.toList.attempt.map {
      case Left(e) => List(s"$name: ${e.getMessage}")
      case Right(entries) =>
        List(
          Option.when(entries.isEmpty)(s"$name: no entries"),
          Option.when(entries.exists(_.title.trim.isEmpty))(s"$name: entries without title"),
          Option.when(entries.map(_.uri).distinct.size != entries.size)(s"$name: repeated links")
        ).flatten
    }

  val tests = Tests {
    test("allFeedsReturnEntries") {
      val found = AllFeeds.feeds.toList.sortBy(_._1).flatTraverse(problems.tupled).unsafeRunSync()
      assert(found.isEmpty)
    }
  }
}
