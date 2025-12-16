package snake.core

import snake.core.GameLogic.{range, grid, withoutLast}

import stainless.lang._
import stainless.math._
import stainless.collection._
import stainless.annotation._

def adjacent(a: Position, b: Position): Boolean =
  (a.x == b.x && abs(a.y - b.y) == 1) ||
    (a.y == b.y && abs(a.x - b.x) == 1)

def continuous(snake: List[Position]): Boolean =
  snake match
    case Cons(h1, Cons(h2, t)) => adjacent(h1, h2) && continuous(Cons(h2, t))
    case _                     => true

def rangeLowerBound(size: BigInt, i: BigInt, x: BigInt): Unit = {
  require(0 <= i && i < size)
  require(range(size, i).contains(x))
  decreases(size - i)
  val tail = if i < size - 1 then range(size, i + 1) else Nil()
  if tail.contains(x) then rangeLowerBound(size, i + 1, x) else ()
}.ensuring(_ => x >= i)

def rangeNoDuplicate(size: BigInt, i: BigInt): Unit = {
  require(0 <= i && i < size)
  decreases(size - i)
  val tail = if i < size - 1 then range(size, i + 1) else Nil()
  if i < size - 1 then
    rangeNoDuplicate(size, i + 1)
    if tail.contains(i) then rangeLowerBound(size, i + 1, i) else ()
  else ()
}.ensuring(ListSpecs.noDuplicate(range(size, i)))

def gridBijection(width: BigInt, i: BigInt, j: BigInt): Unit = {
  require(width > 0)
}.ensuring(_ =>
  val p1 = Position(i % width, i / width)
  val p2 = Position(j % width, j / width)
  ((p1 == p2) ==> (i == j)) && ((i == j) ==> (p1 == p2))
)

def bijInverseContains(
    range: List[BigInt],
    width: BigInt,
    y: Position,
    x: BigInt
): Unit = {
  require(width > 0)
  val bij = (i: BigInt) => Position(i % width, i / width)
  require(range.map(bij).contains(y) && bij(x) == y)
  range match
    case Cons(h, t) =>
      if bij(h) == y then gridBijection(width, h, x)
      else bijInverseContains(t, width, y, x)
    case _ => ()
}.ensuring(range.contains(x))

def gridBijectionNoDuplicate(range: List[BigInt], width: BigInt): Unit = {
  require(width > 0 && ListSpecs.noDuplicate(range))
  val bij = (i: BigInt) => Position(i % width, i / width)
  range match
    case Cons(h, t) =>
      gridBijectionNoDuplicate(t, width)
      if t.map(bij).contains(bij(h)) then
        bijInverseContains(t, width, bij(h), h)
      else ()
    case _ => ()
}.ensuring(ListSpecs.noDuplicate(range.map(i => Position(i % width, i / width))))

def gridNoDuplicate(width: BigInt, height: BigInt): Unit = {
  require(width > 0 && height > 0)
  rangeNoDuplicate(width * height, 0)
  gridBijectionNoDuplicate(range(width * height), width)
}.ensuring(ListSpecs.noDuplicate(grid(width, height)))

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

def removeSubseq[T](l1: List[T], @induct l2: List[T]): Unit = {
}.ensuring(ListSpecs.subseq(l2 -- l1, l2))

def removeCons[T](l1: List[T], @induct l2: List[T]): Unit = {
}.ensuring(_ => 
  l1 match
    case Cons(h, t) => l2 -- l1 == (l2 -- t) -- List(h)
    case _ => true
)

def removeNil[T](@induct l: List[T]): Unit = {
}.ensuring(l -- Nil() == l)

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

def gridWithoutSnakeNonEmpty(state: GameState): Unit = {
  require(validPlayingState(state))

  gridNoDuplicate(state.gridWidth, state.gridHeight)
  noDuplicateFilterLength(state.snake, grid(state.gridWidth, state.gridHeight))

}.ensuring((grid(state.gridWidth, state.gridHeight) -- state.snake).nonEmpty)

def withoutLastLength(@induct s: List[Position]): Unit = {
}.ensuring(s.nonEmpty ==> (s.length - 1 == withoutLast(s).length))

def withoutLastIsSubseq(@induct s: List[Position]): Unit = {
}.ensuring(ListSpecs.subseq(withoutLast(s), s))

def withoutLastContinuous(@induct s: List[Position]): Unit = {
  require(continuous(s))
}.ensuring(continuous(withoutLast(s)))

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
}.ensuring(withinBounds(withoutLast(s), width, height))

def noSelfIntersection(snake: List[Position]): Boolean =
  ListSpecs.noDuplicate(snake)

def withoutLastNoSelfIntersection(s: List[Position]): Unit = {
  require(noSelfIntersection(s))
  withoutLastIsSubseq(s)
  ListSpecs.noDuplicateSubseq(withoutLast(s), s)
}.ensuring(noSelfIntersection(withoutLast(s)))

def validPlayingState(s: GameState): Boolean =
  s.status == GameStatus.Playing &&
    0 < s.snake.length && s.snake.length < s.gridWidth * s.gridHeight &&
    withinBounds(s.snake, s.gridWidth, s.gridHeight) &&
    noSelfIntersection(s.snake) &&
    continuous(s.snake)
