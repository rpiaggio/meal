package com.rpiaggio.meal

import org.http4s.Uri
import enumeratum._

import scala.annotation.tailrec

sealed abstract class ParseAction(val representation: String) extends EnumEntry

object ParseAction extends Enum[ParseAction] {
  override def values = findValues

  final case object Capture extends ParseAction("%")

  final case object Ignore extends ParseAction("*")

  lazy val withRepresentation: Map[String, ParseAction] =
    values.map(action => action.representation -> action).toMap
}

final case class ParseUntil(action: ParseAction, str: String) {
  // Compute Knuth-Morris-Pratt π function for given string.
  lazy val pi: Vector[Int] = {
    (1 until str.length).foldLeft(Vector(0)) {
      case (pi, q) =>
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

final case class ParsePattern(instructions: List[ParseUntil])

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
      .collect {
        case Array(actionStr, until) =>
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
    pageSize: Option[Int] = None
) {
  def parser[F[_]] = EntityParser[F](parsePattern)
  def formatter[F[_]] = EntryCreator[F](entryTemplate, channelEntry.uri)
}

trait FeedList {
  val feeds: Map[String, Feed]
}
