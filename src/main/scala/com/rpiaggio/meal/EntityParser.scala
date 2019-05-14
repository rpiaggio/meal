package com.rpiaggio.meal

import fs2.{Chunk, Pipe, Pull}

object EntityParser {

  private case class ParserState(entryRemainingInstructions: Option[Seq[ParseInstruction]] = None,
                                 currentEntry: EntryData = Seq.empty[String])

  def apply[F[_]](parsePattern: ParsePattern): Pipe[F, String, EntryData] = {

    def go(s: fs2.Stream[F, String], goState: ParserState = ParserState(), previousBuffer: String = ""): Pull[F, EntryData, Unit] = {
      s.pull.uncons.flatMap {
        case Some((head, tail)) =>
          head.map { str =>
            val fullStr = previousBuffer + str

            //                    @tailrec
            def parseChunk(state: ParserState, lastIndex: Int = 0, accum: Seq[EntryData] = Seq.empty): (ParserState, Int, Seq[EntryData]) = {
              val nextString = state.entryRemainingInstructions.fold(parsePattern.start)(_.head.until)
              val nextIndex = fullStr.indexOfSlice(nextString, lastIndex)

              if (nextIndex >= 0) {
                state.entryRemainingInstructions.fold {
                  parseChunk(state.copy(entryRemainingInstructions = Some(parsePattern.instructions)), nextIndex + parsePattern.start.length, accum)
                } { instructions =>
                  val currentInstruction = instructions.head
                  val newCurrentEntry =
                    if (currentInstruction.action == ParseAction.Capture) {
                      state.currentEntry :+ fullStr.slice(lastIndex, nextIndex).replaceAll("[\n\r]", "")
                    } else
                      state.currentEntry
                  val newLastIndex = nextIndex + currentInstruction.until.length

                  val remainingInstructions = instructions.tail
                  if (remainingInstructions.isEmpty) {
                    parseChunk(ParserState(None, Seq.empty[String]), newLastIndex, accum :+ newCurrentEntry)
                  } else {
                    parseChunk(ParserState(Some(remainingInstructions), newCurrentEntry), newLastIndex, accum)
                  }
                }
              } else {
                (state, lastIndex, accum)
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
