package com.rpiaggio.meal

import fs2.{Chunk, Pipe, Pull}

import scala.annotation.tailrec

object EntityParser {

  private case class ParserState(entryRemainingInstructions: List[ParseUntil], currentEntry: EntryData = Nil,
                                 reverseCapture: List[Char] = Nil, currentInstructionMatching: Int = 0)

  def apply[F[_]](parsePattern: ParsePattern): Pipe[F, String, EntryData] = {

    def go(s: fs2.Stream[F, String], goState: ParserState = ParserState(parsePattern.instructions)): Pull[F, EntryData, Unit] = {
      s.pull.uncons.flatMap {
        case Some((head, tail)) =>
          val (newState, outs) =
            head.mapAccumulate(goState) { case (mapState, str) =>
              @tailrec
              def parseStr(state: ParserState, index: Int = 0, reverseAccum: List[EntryData] = Nil): (ParserState, List[EntryData]) = {
                if (index >= str.length) {
                  state.entryRemainingInstructions match {
                    case Nil => (ParserState(parsePattern.instructions), (state.currentEntry +: reverseAccum).reverse)
                    case _ => (state, reverseAccum.reverse)
                  }
                } else {
                  state.entryRemainingInstructions match {
                    case Nil =>
                      parseStr(ParserState(parsePattern.instructions), index, state.currentEntry +: reverseAccum)
                    case currentInstruction :: remainingInstructions =>
                      val currentInstructionStr = currentInstruction.str

                      val newCurrentInstructionMatching = {
                        @tailrec
                        def nextMatching(currentMatching: Int = state.currentInstructionMatching): Int = {
                          if (currentMatching > 0 && str(index) != currentInstructionStr(currentMatching))
                            nextMatching(currentInstruction.pi(currentMatching - 1))
                          else if (str(index) == currentInstructionStr(state.currentInstructionMatching))
                            currentMatching + 1
                          else 0
                        }

                        nextMatching()
                      }

                      val newReverseCapture =
                        if (currentInstruction.action == ParseAction.Capture) {
                          str(index) +: state.reverseCapture
                        } else {
                          Nil
                        }

                      if (newCurrentInstructionMatching == currentInstructionStr.length) {
                        val newCurrentEntry =
                          if (currentInstruction.action == ParseAction.Capture) {
                            newReverseCapture.reverse.dropRight(currentInstructionStr.length).mkString +: state.currentEntry
                          } else {
                            state.currentEntry
                          }
                        parseStr(ParserState(remainingInstructions, newCurrentEntry), index + 1, reverseAccum)
                      } else {
                        parseStr(state.copy(reverseCapture = newReverseCapture, currentInstructionMatching = newCurrentInstructionMatching), index + 1, reverseAccum)
                      }
                  }
                }
              }

              parseStr(mapState)
            }

          Pull.output(outs.flatMap(entries => Chunk(entries:_*))) >> go(tail, newState)
        case None => Pull.done
      }
    }

    in => go(in).stream
  }

}
