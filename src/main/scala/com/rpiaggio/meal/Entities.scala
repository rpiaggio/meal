package com.rpiaggio.meal

import fs2.{Pure, Stream}
import org.http4s.{Headers, Uri}
import org.jsoup.parser.Parser

import scala.annotation.tailrec

enum ParseAction(val representation: String):
  case Capture extends ParseAction("%")
  case Ignore extends ParseAction("*")

object ParseAction:
  lazy val withRepresentation: Map[String, ParseAction] =
    values.map(action => action.representation -> action).toMap

case class ParseUntil(action: ParseAction, str: String) {
  // Compute Knuth-Morris-Pratt π function for given string.
  lazy val pi: Vector[Int] = {
    (1.until(str.length)).foldLeft(Vector(0)) { case (pi, q) =>
      @tailrec
      def nextK(k: Int): Int =
        if (k > 0 && str(k) != str(q))
          nextK(pi(k))
        else if (str(k) == str(q)) k + 1
        else k

      pi :+ nextK(pi.last)
    }
  }
}

case class ParsePattern(instructions: List[ParseUntil])

object ParsePattern {
  private val fakeSeparator = "<!!!!>"
  private val anyParserActionPattern =
    s"\\{(${ParseAction.values.map(a => s"\\${a.representation}").mkString("|")})\\}"

  def apply(pattern: String): ParsePattern = {
    val tokens = pattern
      .replaceAll("[\\n\\r]", "")
      .replaceAll(anyParserActionPattern, s"$fakeSeparator$$1$fakeSeparator")
      .split(fakeSeparator)
    val instructions = tokens.tail
      .grouped(2)
      .collect { case Array(actionStr, until) =>
        ParseUntil(ParseAction.withRepresentation(actionStr), until)
      }
      .toList
    ParsePattern(ParseUntil(ParseAction.Ignore, tokens.head) +: instructions)
  }
}

final case class FeedEntry(title: String, uri: Uri, description: String)

final case class EntryTemplate(
    title: Template,
    link: Template,
    description: Template
)

final case class Feed(
    uriBuilder: UriBuilder,
    channelEntry: FeedEntry,
    parsePattern: ParsePattern,
    entryTemplate: EntryTemplate,
    pageSize: Option[Int] = None,
    followPages: Option[FollowPages] = None
) {
  def parser[F[_]] = EntityParser[F](parsePattern)
  def formatter[F[_]] = EntryCreator[F](entryTemplate, channelEntry.uri)

  // For when the whole page is needed more than once (eg: entries and next link).
  def entries(body: String): List[FeedEntry] =
    Stream.emit(body).through(parser[Pure]).through(formatter[Pure]).toList
}

// Keeps requesting the link captured by `nextPattern` (first capture of first match) until a page
// has no next link or no new entries. Requests share a session, so cookies carry over between pages.
// Entries are deduplicated by link, so pages that repeat earlier results (eg: "load more" responses
// re-rendering the whole list) are handled too.
final case class FollowPages(
    nextPattern: ParsePattern,
    headers: Headers = Headers.empty
) {
  def nextUri(body: String, current: Uri): Option[Uri] =
    Stream
      .emit(body)
      .through(EntityParser[Pure](nextPattern))
      .toList
      .flatMap(_.headOption)
      .headOption
      .flatMap(link => Uri.fromString(Parser.unescapeEntities(link.trim, false)).toOption)
      .map(current.resolve)
}

trait FeedList {
  val feeds: Map[String, Feed]
}
