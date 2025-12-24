package snake.core

import stainless.lang._
import stainless.collection._
import stainless.annotation._

object ListProperties:
  def indexOfCorrectness[T](l: List[T], x: T): Unit = {
    require(l.contains(x))
    l match
      case Cons(h, t) if h != x => indexOfCorrectness(t, x)
      case _                    => ()
  }.ensuring(l(l.indexOf(x)) == x)

  def subseqOfSelf[T](@induct s: List[T]): Unit = {}.ensuring(
    ListSpecs.subseq(s, s)
  )

  def removeSubseq[T](l1: List[T], @induct l2: List[T]): Unit = {}.ensuring(
    ListSpecs.subseq(l2 -- l1, l2)
  )

  def subseqPreservesProperty[T](
      l1: List[T],
      l2: List[T],
      p: T => Boolean
  ): Unit = {
    require(ListSpecs.subseq(l1, l2))
    require(l2.forall(p))
    (l1, l2) match
      case (Cons(x, xs), Cons(y, ys)) =>
        if x == y && ListSpecs.subseq(xs, ys) then
          subseqPreservesProperty(xs, ys, p)
        else subseqPreservesProperty(l1, ys, p)
      case _ => ()
  }.ensuring(l1.forall(p))

  def removeNotPresent[T](@induct l: List[T], x: T): Unit = {
    require(!l.contains(x))
  }.ensuring(l -- List(x) == l)

  def removeLength[T](l: List[T], x: T): Unit = {
    require(ListSpecs.noDuplicate(l))
    l match
      case Cons(h, t) =>
        if h == x then removeNotPresent(t, x)
        else removeLength(t, x)
      case _ => ()
  }.ensuring((l -- List(x)).length >= l.length - 1)

  def removeCons[T](l1: List[T], @induct l2: List[T]): Unit = {}.ensuring(_ =>
    l1 match
      case Cons(h, t) => l2 -- l1 == (l2 -- t) -- List(h)
      case _          => true
  )

  def removeNil[T](@induct l: List[T]): Unit = {}.ensuring(l -- Nil() == l)

  def noDuplicateFilterLength[T](l1: List[T], l2: List[T]): Unit = {
    require(ListSpecs.noDuplicate(l2) && l1.length < l2.length)
    l1 match
      case Cons(h, t) =>
        removeCons(l1, l2)
        removeSubseq(t, l2)
        ListSpecs.noDuplicateSubseq(l2 -- t, l2)
        removeLength(l2 -- t, h)
        noDuplicateFilterLength(t, l2)
      case Nil() => removeNil(l2)
  }.ensuring((l2 -- l1).length >= l2.length - l1.length)
