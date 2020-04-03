package com.rpiaggio.meal

import fs2.Pipe
import org.http4s.Uri
import org.jsoup.Jsoup

object EntryCreator {

  private val parameterPattern = "\\{%(\\d+)\\}".r

  def apply[F[_]](entryTemplate: EntryTemplate, baseUri: Uri): Pipe[F, EntryData, FeedEntry] = {
    in =>
      in.map { seq =>
        def replace(template: String): String = {
          parameterPattern.replaceAllIn(template, mtch => seq(mtch.group(1).toInt - 1).trim)
        }

        FeedEntry(
          Jsoup.parse(replace(entryTemplate.title)).body.text,
          Uri.fromString(replace(entryTemplate.link)).map(baseUri.resolve).getOrElse(baseUri).toString,
          replace(entryTemplate.description)
        )
      }
  }
}
