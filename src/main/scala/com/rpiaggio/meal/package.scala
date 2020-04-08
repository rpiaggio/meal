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

    def asQueryParamsRange(uri: Uri, itemsPerPage: Int, fromKey: String = "from", countKey: String = "count"): UriBuilder =
      page => uri.withQueryParam(fromKey, (page - 1) * itemsPerPage).withQueryParam(countKey, itemsPerPage)
  }

  protected[meal] val MaxRedirCount = 20
}
