package snake.core

import snake.core.Grid._

import stainless.lang._
import stainless.math._
import stainless.collection._
import stainless.annotation._

object Properties:
  /** Predicate describing a well-formed `Playing` state:
    *   - status is `Playing`
    *   - snake length is positive and strictly less than total grid cells
    *   - all positions are within bounds, no duplicates, and the body is contiguous
    */
  def validPlayingState(state: GameState): Boolean =
    state.status == GameStatus.Playing &&
      0 < state.snake.length && state.snake.length < state.gridWidth * state.gridHeight &&
      allWithinBounds(state.snake, state.gridWidth, state.gridHeight) &&
      noSelfIntersection(state.snake) &&
      contiguous(state.snake)

  /** Predicate describing admissible transitions from a valid playing state:
    *   - snake length never decreases
    *   - snake length grows by at most one
    *   - reaching `GameWon` is equivalent to filling the grid
    *   - unless the game is `GameOver`, the new snake (minus head) must be a subsequence of the
    *     previous snake (movement preserves body)
    */
  def validTransistion(curr: GameState, next: GameState): Boolean =
    require(validPlayingState(curr))
    curr.snake.length <= next.snake.length &&
    next.snake.length <= curr.snake.length + 1 &&
    (next.status == GameStatus.GameWon) ==> (next.snake.length == next.gridWidth * next.gridHeight) &&
    (next.snake.length == next.gridWidth * next.gridHeight) ==> (next.status == GameStatus.GameWon) &&
    next.status != GameStatus.GameOver ==> ListSpecs.subseq(next.snake.tail, curr.snake)

  def allWithinBounds(
      snake: List[Position],
      width: BigInt,
      height: BigInt
  ): Boolean =
    snake.forall(isWithinBounds(width, height))

  def noSelfIntersection(snake: List[Position]): Boolean =
    ListSpecs.noDuplicate(snake)

  def contiguous(snake: List[Position]): Boolean =
    def adjacent(a: Position, b: Position): Boolean =
      (a.x == b.x && abs(a.y - b.y) == 1) ||
        (a.y == b.y && abs(a.x - b.x) == 1)

    snake match
      case Cons(h1, Cons(h2, t)) => adjacent(h1, h2) && contiguous(Cons(h2, t))
      case _                     => true
