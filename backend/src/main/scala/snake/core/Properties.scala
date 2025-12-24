package snake.core

import snake.core.Grid._

import stainless.lang._
import stainless.math._
import stainless.collection._
import stainless.annotation._

object Properties:
  def validTransistion(curr: GameState, next: GameState): Boolean =
    require(validPlayingState(curr))
    curr.snake.length <= next.snake.length &&
    next.snake.length <= curr.snake.length + 1 &&
    (next.status == GameStatus.GameWon) ==> (next.snake.length == next.gridWidth * next.gridHeight) &&
    (next.snake.length == next.gridWidth * next.gridHeight) ==> (next.status == GameStatus.GameWon) &&
    next.status != GameStatus.GameOver ==> ListSpecs.subseq(next.snake.tail, curr.snake)

  def validPlayingState(state: GameState): Boolean =
    state.status == GameStatus.Playing &&
      0 < state.snake.length && state.snake.length < state.gridWidth * state.gridHeight &&
      allWithinBounds(state.snake, state.gridWidth, state.gridHeight) &&
      noSelfIntersection(state.snake) &&
      continuous(state.snake)

  def allWithinBounds(
      snake: List[Position],
      width: BigInt,
      height: BigInt
  ): Boolean =
    snake.forall(isWithinBounds(width, height))

  def noSelfIntersection(snake: List[Position]): Boolean =
    ListSpecs.noDuplicate(snake)

  def continuous(snake: List[Position]): Boolean =
    def adjacent(a: Position, b: Position): Boolean =
      (a.x == b.x && abs(a.y - b.y) == 1) ||
        (a.y == b.y && abs(a.x - b.x) == 1)

    snake match
      case Cons(h1, Cons(h2, t)) => adjacent(h1, h2) && continuous(Cons(h2, t))
      case _                     => true
