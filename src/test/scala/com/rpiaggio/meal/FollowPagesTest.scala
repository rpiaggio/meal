package com.rpiaggio.meal

import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.headers.Location
import org.http4s.implicits._
import org.typelevel.ci._
import utest._

import java.util.concurrent.atomic.AtomicInteger

// Runs the paging logic against an in-memory site.
object FollowPagesTest extends TestSuite {
  private val host = uri"http://test.local"

  private def page(items: Seq[String], next: Option[String] = None): IO[Response[IO]] =
    Ok(items.map(i => s"""<i href="/item/$i">$i</i>""").mkString + next.fold("")(n => s"""next="$n""""))

  private def feed(start: String, headers: Headers = Headers.empty): Feed =
    Feed(
      Page.single(host.withPath(Uri.Path.unsafeFromString(start))),
      FeedEntry("Test", host, "Test"),
      ParsePattern("""<i href="{%}">{%}</i>"""),
      EntryTemplate("{%2}", "{%1}", ""),
      followPages = Some(FollowPages(ParsePattern("""next="{%}""""), headers))
    )

  // Returns the titles of the entries, and how many requests the site got.
  private def run(feed: Feed)(routes: PartialFunction[Request[IO], IO[Response[IO]]]): (List[String], Int) = {
    val requests = new AtomicInteger(0)
    val app = HttpRoutes
      .of[IO](routes)
      .orNotFound
      .local[Request[IO]] { req => requests.incrementAndGet(); req }
    val builder = new FeedBuilder[IO](new HttpClient[IO](Resource.pure(Client.fromHttpApp(app))))
    val titles = builder.feedStream(feed).compile.toList.unsafeRunSync().map(_.title)
    (titles, requests.get)
  }

  val tests = Tests {
    test("followsNextLinksUntilThereAreNone") {
      val result = run(feed("/p/1")) {
        case GET -> Root / "p" / "1" => page(Seq("a1", "a2"), Some("/p/2"))
        case GET -> Root / "p" / "2" => page(Seq("b1", "b2"), Some("3")) // Relative to the current page.
        case GET -> Root / "p" / "3" => page(Seq("c1"))
      }
      assert(result == (List("a1", "a2", "b1", "b2", "c1"), 3))
    }

    test("cumulativePagesEmitEachEntryOnce") {
      val result = run(feed("/c/1")) { case GET -> Root / "c" / IntVar(n) =>
        page((1 to 2 * n).map(i => s"e$i"), Option.when(n < 3)(s"/c/${n + 1}"))
      }
      assert(result == ((1 to 6).map(i => s"e$i").toList, 3))
    }

    test("stopsWhenAPageHasNoNewEntries") {
      val result = run(feed("/same")) { case GET -> Root / "same" =>
        page(Seq("x1"), Some("/same"))
      }
      assert(result == (List("x1"), 2))
    }

    test("stopsAtMaxFollowedPages") {
      val (titles, requests) = run(feed("/inf/1")) { case GET -> Root / "inf" / IntVar(n) =>
        page(Seq(s"n$n"), Some(s"/inf/${n + 1}"))
      }
      assert(titles.size == MaxFollowedPages, requests == MaxFollowedPages)
    }

    test("keepsSessionCookieAndSendsHeadersOnFollowUps") {
      val followHeader = Header.Raw(ci"X-Follow", "yes")
      val result = run(feed("/start", Headers(followHeader))) {
        // The session is set mid-redirect, like TickAntel does.
        case GET -> Root / "start" =>
          IO.pure(
            Response[IO](Status.Found)
              .putHeaders(Location(host / "list"))
              .addCookie("sid", "abc")
          )
        case req @ GET -> Root / "list" =>
          if (req.headers.get(ci"X-Follow").isDefined) BadRequest()
          else page(Seq("l1"), Some("/more"))
        case req @ GET -> Root / "more" =>
          val hasSession = req.cookies.exists(c => c.name == "sid" && c.content == "abc")
          val hasHeader = req.headers.get(ci"X-Follow").isDefined
          if (hasSession && hasHeader) page(Seq("m1")) else Forbidden()
      }
      assert(result == (List("l1", "m1"), 3))
    }

    test("failsWhenAFollowUpFails") {
      val error = assertThrows[Exception] {
        run(feed("/ok")) {
          case GET -> Root / "ok"     => page(Seq("o1"), Some("/broken"))
          case GET -> Root / "broken" => InternalServerError()
        }
      }
      assert(error.getMessage.contains("Failed to fetch [http://test.local/broken]"))
    }
  }
}
