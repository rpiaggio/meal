package com.rpiaggio.meal

import java.nio.charset.StandardCharsets

import org.http4s.Uri
import enumeratum._
import fs2.{Chunk, Pipe, Pull}

import scala.annotation.tailrec

sealed abstract class ParseAction(val representation: String) extends EnumEntry

object ParseAction extends Enum[ParseAction] {
  override def values = findValues

  final case object Capture extends ParseAction("%")

  final case object Ignore extends ParseAction("*")

  lazy val withRepresentation: Map[String, ParseAction] = values.map(action => action.representation -> action).toMap
}

final case class ParseInstruction(action: ParseAction, until: String)

final case class ParsePattern(start: String, doingFirst: ParseInstruction, andThen: ParseInstruction*) {
  val instructions = doingFirst +: andThen
}

object ParsePattern {
  private val fakeSeparator = "<!!!!>"
  private val anyParserActionPattern = s"\\{(${ParseAction.values.map(a => s"\\${a.representation}").mkString("|")})\\}"

  def apply(pattern: String): ParsePattern = {
    val tokens = pattern
      .replaceAll("[\\n\\r]", "")
      .replaceAll(anyParserActionPattern, s"$fakeSeparator$$1$fakeSeparator")
      .split(fakeSeparator)
    val instructions = tokens.tail.grouped(2).collect { case Array(actionStr, until) => ParseInstruction(ParseAction.withRepresentation(actionStr), until) }.toSeq
    ParsePattern(tokens.head, instructions.head, instructions.tail: _*)
  }
}

// TODO CONTEMPLATE URI PARSING FAILURE
final case class FeedEntry(title: String, link: String, description: String) {
  lazy val uris: Stream[Uri] =
    if (link.contains("$page"))
      Stream(1 to PAGES_REQUEST: _*).map { page =>
        Uri.fromString(link.replace("$page", page.toString)).right.get
      }
    else
      Stream(Uri.fromString(link).right.get)
}

final case class Feed(channelEntry: FeedEntry, parsePattern: ParsePattern, entryTemplate: FeedEntry) {
  def parser[F[_]] = EntityParser[F](parsePattern)
  def formatter[F[_]] = EntryCreator[F](entryTemplate, channelEntry.uris.head)

  //  lazy val formatter: Flow[EntryData, FeedEntry, Any] = Flow.fromFunction(new EntryCreator(entryTemplate, channelEntry.uris.head))
}

trait FeedList {
  val feeds: Map[String, Feed]
}