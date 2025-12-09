package snake.core

import stainless.lang._
import stainless.collection._
import stainless.annotation._

def adjacent(a: Position, b: Position): Boolean =
  def abs(x: BigInt): BigInt = if x >= 0 then x else -x

  (a.x == b.x && abs(a.y - b.y) == 1) ||
  (a.y == b.y && abs(a.x - b.x) == 1)

def continuous(snake: List[Position]): Boolean =
  snake match
    case Cons(h1, Cons(h2, t)) => adjacent(h1, h2) && continuous(Cons(h2, t))
    case _                     => true

def withoutLast(s: List[Position]): List[Position] = s match
  case Cons(h, t @ Cons(_, _)) => Cons(h, withoutLast(t))
  case _                       => Nil()

def withoutLastIsSubseq(@induct s: List[Position]): Unit = {}.ensuring(_ =>
  ListSpecs.subseq(withoutLast(s), s)
)

def withoutLastContinuous(@induct s: List[Position]): Unit = {
  require(continuous(s))
}.ensuring(_ => continuous(withoutLast(s)))

def withinBounds(
    snake: List[Position],
    width: BigInt,
    height: BigInt
): Boolean =
  snake.forall(p => 0 <= p.x && p.x < width && 0 <= p.y && p.y < height)

def withoutLastWithinBounds(
    @induct s: List[Position],
    width: BigInt,
    height: BigInt
): Unit = {
  require(withinBounds(s, width, height))
}.ensuring(_ => withinBounds(withoutLast(s), width, height))

def noSelfIntersection(snake: List[Position]): Boolean =
  ListSpecs.noDuplicate(snake)

def withoutLastNoSelfIntersection(s: List[Position]): Unit = {
  require(noSelfIntersection(s))
  withoutLastIsSubseq(s)
  ListSpecs.noDuplicateSubseq(withoutLast(s), s)
}.ensuring(_ => noSelfIntersection(withoutLast(s)))

def validPlayingState(s: GameState): Boolean =
  s.status == GameStatus.Playing &&
    s.snake.nonEmpty &&
    withinBounds(s.snake, s.gridWidth, s.gridHeight) &&
    noSelfIntersection(s.snake) &&
    continuous(s.snake)
