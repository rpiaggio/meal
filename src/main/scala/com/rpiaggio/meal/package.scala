package com.rpiaggio

import org.http4s.Uri

package object meal {
  type EntryData = List[String]

  type Page = Int

  type UriBuilder = Page => Uri

  object Page {
    def single(uri: Uri): UriBuilder =
      _ => uri

    def inPath(uri: Uri, pattern: String = "$page"): UriBuilder =
      page => uri.withPath(uri.path.replace(pattern, page.toString))

    def asQueryParam(uri: Uri, key: String = "page"): UriBuilder =
      page => uri.withQueryParam(key, page)
  }

  protected[meal] val PAGES_REQUEST = 10

  protected[meal] val MAX_REDIR_COUNT = 20
}
