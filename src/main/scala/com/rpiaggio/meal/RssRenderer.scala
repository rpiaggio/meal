package com.rpiaggio.meal

import fs2.{Chunk, Pipe, Stream}

object RssRenderer {
  private def prefix[F[_]](channelEntry: FeedEntry): Stream[F, String] =
    Stream.emit(
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<?xml-stylesheet type=\"text/xsl\" href=\"../xsl\"?>\n" +
        "<rss version=\"2.0\">\n" +
        "<channel>\n" +
        <title>{channelEntry.title}</title>.toString +
        <link>{channelEntry.uri}</link>.toString +
        <description>{channelEntry.description}</description>.toString + "\n"
    )

  private def bodyRenderer[F[_]]: Pipe[F, FeedEntry, String] =
    in =>
      in.map { entry =>
        val node =
          <item>
          <title>{entry.title}</title>
          <link>{entry.uri}</link>
          <description>{entry.description}</description>
        </item>

        node.toString + "\n"
      }

  private def suffix[F[_]]: Stream[F, String] = Stream.emit(
    "</channel>\n" +
      "</rss>"
  )

  def apply[F[_]](
      channelEntry: FeedEntry,
      bodyStream: Stream[F, FeedEntry]
  ): Stream[F, Chunk[Byte]] =
    (prefix[F](channelEntry) ++ bodyStream
      .through(bodyRenderer[F]) ++ suffix[F]).through(fs2.text.utf8EncodeC)
}
