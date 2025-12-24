package snake.core

import stainless.lang._
import stainless.collection._

object ListUtils:
  def withoutLast[T](l: List[T]): List[T] = l match
    case Cons(h, t @ Cons(_, _)) => Cons(h, withoutLast(t))
    case _                       => Nil()
