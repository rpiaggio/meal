package com.rpiaggio.meal

import fs2.Pipe
import org.http4s.Uri
import org.jsoup.Jsoup

object EntryCreator {

  def apply[F[_]](
      entryTemplate: EntryTemplate,
      baseUri: Uri
  ): Pipe[F, EntryData, FeedEntry] = { in =>
    in.map { entryData =>
      FeedEntry(
        Jsoup.parse(entryTemplate.title(entryData)).body.text,
        Uri
          .fromString(entryTemplate.link(entryData))
          .map(baseUri.resolve)
          .getOrElse(baseUri),
        entryTemplate.description(entryData)
      )
    }
  }
}
