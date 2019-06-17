package com.rpiaggio.meal

import fs2.{Chunk, Pipe, Pull}

import scala.annotation.tailrec

object EntityParser {

  private case class ParserState(entryRemainingInstructions: List[ParseUntil], currentEntry: EntryData = Seq.empty[String])

  def apply[F[_]](parsePattern: ParsePattern): Pipe[F, String, EntryData] = {

    def go(s: fs2.Stream[F, String], goState: ParserState = ParserState(parsePattern.instructions), previousBuffer: String = ""): Pull[F, EntryData, Unit] = {
      s.pull.uncons.flatMap {
        case Some((head, tail)) =>
          head.map { str =>
            val fullStr = previousBuffer + str

            @tailrec
            def parseChunk(state: ParserState, lastIndex: Int = 0, accum: Seq[EntryData] = Seq.empty): (ParserState, Int, Seq[EntryData]) = {
              state.entryRemainingInstructions match {
                case Nil =>
                  parseChunk(ParserState(parsePattern.instructions), lastIndex, accum :+ state.currentEntry)
                case currentInstruction :: remainingInstructions =>

                  val nextString = currentInstruction.str
                  val nextIndex = fullStr.indexOfSlice(nextString, lastIndex)

                  if (nextIndex >= 0) {
                    val newCurrentEntry =
                      if (currentInstruction.action == ParseAction.Capture) {
                        state.currentEntry :+ fullStr.slice(lastIndex, nextIndex).replaceAll("[\n\r]", "")
                      } else {
                        state.currentEntry
                      }
                    val newLastIndex = nextIndex + currentInstruction.str.length

                    parseChunk(ParserState(remainingInstructions, newCurrentEntry), newLastIndex, accum)
                  } else {
                    (state, lastIndex, accum)
                  }
              }
            }

            val (newState, lastIndex, outs) = parseChunk(goState)
            Pull.output(Chunk(outs: _*)) >> go(tail, newState, fullStr.drop(lastIndex))
          }.head.get
        case None => Pull.done
      }
    }

    in => go(in).stream
  }

}
