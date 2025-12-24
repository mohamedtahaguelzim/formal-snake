package snake.core

import snake.core.Grid._
import snake.core.ListUtils._
import snake.core.Properties._

import stainless.lang._
import stainless.math._
import stainless.collection._
import stainless.annotation._

object ListUtilsProperties:
  def withoutLastLength[T](@induct s: List[T]): Unit = {}.ensuring(
    s.nonEmpty ==> (s.length - 1 == withoutLast(s).length)
  )

  def withoutLastIsSubseq[T](@induct s: List[T]): Unit = {}.ensuring(
    ListSpecs.subseq(withoutLast(s), s)
  )

  def withoutLastWithinBounds(
      @induct s: List[Position],
      width: BigInt,
      height: BigInt
  ): Unit = {
    require(allWithinBounds(s, width, height))
  }.ensuring(allWithinBounds(withoutLast(s), width, height))

  def withoutLastContiguous(@induct s: List[Position]): Unit = {
    require(contiguous(s))
  }.ensuring(contiguous(withoutLast(s)))

  def withoutLastNoSelfIntersection(s: List[Position]): Unit = {
    require(noSelfIntersection(s))
    withoutLastIsSubseq(s)
    ListSpecs.noDuplicateSubseq(withoutLast(s), s)
  }.ensuring(noSelfIntersection(withoutLast(s)))
