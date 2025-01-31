package com.rpiaggio.meal

import fs2.{Chunk, Pipe, Pull}

import scala.annotation.tailrec

object EntityParser {

  private case class ParserState(
      entryRemainingInstructions: List[ParseUntil],
      reverseCurrentEntry: EntryData = Nil,
      reverseCapture: List[Char] = Nil,
      currentInstructionMatching: Int = 0
  )

  def apply[F[_]](parsePattern: ParsePattern): Pipe[F, String, EntryData] = {

    def go(
        s: fs2.Stream[F, String],
        goState: ParserState = ParserState(parsePattern.instructions)
    ): Pull[F, EntryData, Unit] = {
      s.pull.uncons.flatMap {
        case Some((head, tail)) =>
          val (newState, outs) =
            head.mapAccumulate(goState) { case (mapState, str) =>
              val strLength = str.length

//              println(s"New str: [$str]")

              @tailrec
              def parseStr(
                  state: ParserState,
                  index: Int = 0,
                  reverseAccum: List[EntryData] = Nil
              ): (ParserState, List[EntryData]) = {

//                println(s"parseStr(state: $state, index: $index, reverseAccum: $reverseAccum")

                if (index >= strLength) {
                  state.entryRemainingInstructions match {
                    case Nil =>
                      (
                        ParserState(parsePattern.instructions),
                        (state.reverseCurrentEntry.reverse +: reverseAccum).reverse
                      )
                    case _ => (state, reverseAccum.reverse)
                  }
                } else {
                  state.entryRemainingInstructions match {
                    case Nil =>
                      parseStr(
                        ParserState(parsePattern.instructions),
                        index,
                        state.reverseCurrentEntry.reverse +: reverseAccum
                      )
                    case currentInstruction :: remainingInstructions =>
                      val currentInstructionStr = currentInstruction.str
                      val currentLength = currentInstructionStr.length

                      val (
                        newIndex,
                        newCurrentInstructionMatching,
                        newReverseCapture
                      ) = {
                        @tailrec
                        def nextMatching(
                            i: Int = index,
                            currentMatching: Int =
                              state.currentInstructionMatching,
                            currentCapture: List[Char] = state.reverseCapture
                        ): (Int, Int, List[Char]) = {

//                          println(s"nextMatching(i: $i, currentMatching: $currentMatching, currentCapture: $currentCapture")

                          def doCapture() = // We capture a character every time we advance the index i.
                            if (
                              currentInstruction.action == ParseAction.Capture
                            ) {
                              str(i) +: currentCapture
                            } else {
                              Nil
                            }

                          if (
                            i == strLength || currentMatching == currentLength
                          ) // We only leave this loop if there's a match or the string is exhausted.
                            (i, currentMatching, currentCapture)
                          else if (
                            str(i) == currentInstructionStr(currentMatching)
                          )
                            nextMatching(
                              i + 1,
                              currentMatching + 1,
                              doCapture()
                            )
                          else if (
                            currentMatching > 0
                          ) // str(index) != currentInstructionStr(currentMatching)
                            nextMatching(
                              i,
                              currentInstruction.pi(currentMatching - 1)
                            )
                          else nextMatching(i + 1, 0, doCapture())
                        }

                        nextMatching()
                      }

                      if (newCurrentInstructionMatching == currentLength) { // There's a match, continue parsing string. String may or may not be exhausted.
                        val newCurrentEntry =
                          if (
                            currentInstruction.action == ParseAction.Capture
                          ) {
                            newReverseCapture.reverse
                              .dropRight(currentLength)
                              .mkString +: state.reverseCurrentEntry
                          } else {
                            state.reverseCurrentEntry
                          }
                        parseStr(
                          ParserState(remainingInstructions, newCurrentEntry),
                          newIndex,
                          reverseAccum
                        )
                      } else { // String exhausted. Loop once more to apply wrap-up logic at the beginning of this function.
                        parseStr(
                          state.copy(
                            reverseCapture = newReverseCapture,
                            currentInstructionMatching =
                              newCurrentInstructionMatching
                          ),
                          newIndex,
                          reverseAccum
                        )
                      }
                  }
                }
              }

              parseStr(mapState)
            }

          Pull.output(outs.flatMap(entries => Chunk(entries *))) >> go(
            tail,
            newState
          )
        case None => Pull.done
      }
    }

    in => go(in).stream
  }

}
