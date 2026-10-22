package com.rpiaggio.meal.uy

import com.rpiaggio.meal._
import org.http4s.implicits._
import utest._

object TickAntelTest extends TestSuite {
  private val feed = TickAntel.feeds("tickantel-musica")
  private val follow = feed.followPages.get

  private val pageUri = uri"https://tickantel.com.uy/inicio/buscar_categoria?0&cat_id=2"
  private val loadMoreUri =
    uri"https://tickantel.com.uy/inicio/buscar_categoria?0-1.IBehaviorListener.0-cargarMasResultadosPanel&cat_id=2"

  private def entry(entries: List[FeedEntry], path: String): FeedEntry =
    entries.find(_.uri.renderString.endsWith(path)).getOrElse(throw new Exception(s"No entry for [$path]"))

  val tests = Tests {
    test("firstPage") {
      val entries = feed.entries(Fixtures("tickantel-first.html"))
      assert(entries.size == 6)
      assert(
        entries.head == FeedEntry(
          "Ave Fénix – Cuarteto de Cuerdas (22 oct 2026)",
          uri"https://tickantel.com.uy/ng/espectaculo/40021484/ave-fenix-cuarteto-de-cuerdas",
          "Complejo Cultural Politeama - Sala Atahualpa del Cioppo"
        )
      )
      assert(follow.nextUri(Fixtures("tickantel-first.html"), pageUri) == Some(loadMoreUri))
    }

    test("loadMoreResponse") {
      val body = Fixtures("tickantel-ajax.xml")
      val entries = feed.entries(body)
      // Responses re-render the whole list so far.
      assert(entries.size == 145)
      assert(entries.take(6) == feed.entries(Fixtures("tickantel-first.html")))
      assert(follow.nextUri(body, loadMoreUri) == Some(loadMoreUri))
    }

    test("cardVariants") {
      val entries = feed.entries(Fixtures("tickantel-ajax.xml"))
      val twoDates = entry(entries, "/40021815/concierto-musa-2026")
      assert(twoDates.title == "Concierto Musa 2026 (del 31 oct 2026 al 01 nov 2026)")
      val until = entry(entries, "/40021449/buitres-nido-de-espinas-sesiones-inmersivas")
      assert(until.title == "Buitres - Nido de Espinas – Sesiones Inmersivas (hasta el 28 nov 2026)")
      val subtitle = entry(entries, "/40021613/los-fatales")
      assert(subtitle.title == "Los Fatales - 30 años (22 nov 2026)")
      val noDate = entry(entries, "/40020866/florencia-nunez-rio-negro-reprogramado")
      assert(noDate.title == "Florencia Núñez - Río Negro - REPROGRAMADO")
      // Venue and room are the same here, so it's shown once.
      assert(subtitle.description == "Antel Arena")
      val abono = entry(entries, "/abono/40000727/abono-ciclo-musica-de-camara-2026")
      assert(abono.description == "Teatro Solís - Delmira Agustini")
    }

    test("endOfResults") {
      val body = Fixtures("tickantel-ajax-end.xml")
      assert(feed.entries(body).isEmpty)
      assert(follow.nextUri(body, loadMoreUri).isEmpty)
    }
  }
}
