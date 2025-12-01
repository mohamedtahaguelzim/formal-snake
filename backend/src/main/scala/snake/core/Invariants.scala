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
    case Nil()            => true
    case Cons(_, Nil())   => true
    case Cons(h1, Cons(h2, t)) =>
      adjacent(h1, h2) && continuous(Cons(h2, t))

def withoutLast(s: List[Position]): List[Position] = s match
  case Nil()               => Nil()
  case Cons(_, Nil())      => Nil()
  case Cons(h, t)          => Cons(h, withoutLast(t))

def withoutLastContinuous(s: List[Position]): Boolean = {
  require(continuous(s))
  s match
    case Nil() =>
      true
    case Cons(_, Nil()) =>
      true
    case Cons(h1, t @ Cons(h2, _)) =>
      withoutLastContinuous(t)
}.ensuring(_ => 
    continuous(withoutLast(s))
)

def validPlayingState(s: GameState): Boolean =
    s.status == GameStatus.Playing &&
    s.snake.nonEmpty &&
    continuous(s.snake)
