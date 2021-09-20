package com.rpiaggio

import org.http4s.Uri

package object meal {

  import java.time.ZoneId

  import java.{util => ju}
  type EntryData = List[String]

  trait Template extends (EntryData => String) {
    def mapAt(index: Int, f: String => String): Template =
      list => this(list.updated(index - 1, f(list(index - 1))))
  }

  type Page = Int

  type UriBuilder = Page => Uri

  object Template {
    private val parameterPattern = "\\{%(\\d+)\\}".r

    implicit def stringTemplate(str: String): Template =
      list =>
        parameterPattern.replaceAllIn(
          str,
          mtch =>
            list(mtch.group(1).toInt - 1).trim.replaceAll("""\\""", """\\\\""")
        )
  }

  implicit class TemplateStrOps(s: String) {
    def mapAt(index: Int, f: String => String): Template =
      (s: Template).mapAt(index, f)
  }

  object Page {
    def single(uri: Uri): UriBuilder =
      _ => uri

    def inPath(uri: Uri, pattern: String = "$page"): UriBuilder =
      page =>
        uri.withPath(
          Uri.Path.unsafeFromString(
            uri.path.toString.replace(pattern, page.toString)
          )
        )

    def asQueryParam(uri: Uri, key: String = "page"): UriBuilder =
      page => uri.withQueryParam(key, page)

    def asQueryParamsRange(
        uri: Uri,
        itemsPerPage: Int,
        fromKey: String = "from",
        countKey: String = "count"
    ): UriBuilder =
      page =>
        uri
          .withQueryParam(fromKey, (page - 1) * itemsPerPage)
          .withQueryParam(countKey, itemsPerPage)
  }

  protected[meal] val MaxRedirCount = 20

  protected[meal] val Timezone = ZoneId.of("America/Montevideo")
}
