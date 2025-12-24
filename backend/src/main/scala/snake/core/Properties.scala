package snake.core

import snake.core.GameLogic.{range, grid, withoutLast}

import stainless.lang._
import stainless.math._
import stainless.collection._
import stainless.annotation._

def validTransistion(prev: GameState, next: GameState): Boolean =
  require(validPlayingState(prev))
  prev.snake.length <= next.snake.length &&
    next.snake.length <= prev.snake.length + 1 &&
    (next.status == GameStatus.GameWon) ==> (next.snake.length == next.gridWidth * next.gridHeight) &&
    (next.snake.length == next.gridWidth * next.gridHeight) ==> (next.status == GameStatus.GameWon) &&
    next.status != GameStatus.GameOver ==> ListSpecs.subseq(next.snake.tail, prev.snake)

def validPlayingState(s: GameState): Boolean =
  s.status == GameStatus.Playing &&
    0 < s.snake.length && s.snake.length < s.gridWidth * s.gridHeight &&
    withinBounds(s.snake, s.gridWidth, s.gridHeight) &&
    noSelfIntersection(s.snake) &&
    continuous(s.snake)

def withinBounds(
    snake: List[Position],
    width: BigInt,
    height: BigInt
): Boolean =
  snake.forall(p => 0 <= p.x && p.x < width && 0 <= p.y && p.y < height)

def noSelfIntersection(snake: List[Position]): Boolean =
  ListSpecs.noDuplicate(snake)

def adjacent(a: Position, b: Position): Boolean =
  (a.x == b.x && abs(a.y - b.y) == 1) ||
    (a.y == b.y && abs(a.x - b.x) == 1)

def continuous(snake: List[Position]): Boolean =
  snake match
    case Cons(h1, Cons(h2, t)) => adjacent(h1, h2) && continuous(Cons(h2, t))
    case _                     => true

// ========== Generated Food is Within Bound ==========
def indexOfCorrectness[T](l: List[T], x: T): Unit = {
  require(l.contains(x))
  l match
    case Cons(h, t) if h != x => indexOfCorrectness(t, x)
    case _                    => ()
}.ensuring(l(l.indexOf(x)) == x)

def rangeLowerBound(right: BigInt, left: BigInt, x: BigInt): Unit = {
  require(left < right)
  require(range(right, left).contains(x))
  decreases(right - left)
  val tail = if left < right - 1 then range(right, left + 1) else Nil()
  if tail.contains(x) then rangeLowerBound(right, left + 1, x) else ()
}.ensuring(left <= x)

def rangeBounds(right: BigInt, left: BigInt, x: BigInt): Unit = {
  require(left < right)
  val l = range(right, left)
  require(l.contains(x))
  indexOfCorrectness(l, x)
  ListSpecs.applyForAll(l, l.indexOf(x), _ < right)
  rangeLowerBound(right, left, x)
}.ensuring(left <= x && x < right)

def mappingBounds(width: BigInt, height: BigInt, i: BigInt): Unit = {
  require(width > 0 && height > 0)
  require(0 <= i && i < width * height)
}.ensuring(
  0 <= i % width && i % width < width &&
    0 <= i / width && i / width < height
)

def rangeMappingWithinBound(
    right: BigInt,
    left: BigInt,
    width: BigInt,
    height: BigInt
): Unit = {
  require(width > 0 && height > 0)
  require(0 <= left && left < right && right <= width * height)
  decreases(right - left)
  mappingBounds(width, height, left)
  if left < right - 1 then
    rangeMappingWithinBound(right, left + 1, width, height)
  else ()
}.ensuring(
  range(right, left)
    .map(i => Position(i % width, i / width))
    .forall(p => 0 <= p.x && p.x < width && 0 <= p.y && p.y < height)
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

// ========== Grid Without Snake is NonEmpty ==========
def rangeNoDuplicate(size: BigInt, i: BigInt): Unit = {
  require(0 <= i && i < size)
  decreases(size - i)
  val tail = if i < size - 1 then range(size, i + 1) else Nil()
  if i < size - 1 then
    rangeNoDuplicate(size, i + 1)
    if tail.contains(i) then rangeLowerBound(size, i + 1, i) else ()
  else ()
}.ensuring(ListSpecs.noDuplicate(range(size, i)))

def gridInjection(width: BigInt, i: BigInt, j: BigInt): Unit = {
  require(width > 0)
}.ensuring(_ =>
  val p1 = Position(i % width, i / width)
  val p2 = Position(j % width, j / width)
  (p1 == p2) ==> (i == j)
)

def injInverseContains(
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
      if bij(h) == y then gridInjection(width, h, x)
      else injInverseContains(t, width, y, x)
    case _ => ()
}.ensuring(range.contains(x))

def gridInjectionNoDuplicate(range: List[BigInt], width: BigInt): Unit = {
  require(width > 0 && ListSpecs.noDuplicate(range))
  val bij = (i: BigInt) => Position(i % width, i / width)
  range match
    case Cons(h, t) =>
      gridInjectionNoDuplicate(t, width)
      if t.map(bij).contains(bij(h)) then
        injInverseContains(t, width, bij(h), h)
      else ()
    case _ => ()
}.ensuring(ListSpecs.noDuplicate(range.map(i => Position(i % width, i / width))))

def gridNoDuplicate(width: BigInt, height: BigInt): Unit = {
  require(width > 0 && height > 0)
  rangeNoDuplicate(width * height, 0)
  gridInjectionNoDuplicate(range(width * height), width)
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

def gridWithoutSnakeNonEmpty(state: GameState): Unit = {
  require(validPlayingState(state))
  gridNoDuplicate(state.gridWidth, state.gridHeight)
  noDuplicateFilterLength(state.snake, grid(state.gridWidth, state.gridHeight))
}.ensuring((grid(state.gridWidth, state.gridHeight) -- state.snake).nonEmpty)

// ========== Snake Length bounds ==========
def withoutLastLength(@induct s: List[Position]): Unit = {
}.ensuring(s.nonEmpty ==> (s.length - 1 == withoutLast(s).length))

// ========== Snake is within grid bounds ==========
def withoutLastWithinBounds(
    @induct s: List[Position],
    width: BigInt,
    height: BigInt
): Unit = {
  require(withinBounds(s, width, height))
}.ensuring(withinBounds(withoutLast(s), width, height))

// ========== Snake is Continuous ==========
def withoutLastContinuous(@induct s: List[Position]): Unit = {
  require(continuous(s))
}.ensuring(continuous(withoutLast(s)))

// ========== Snake has no self intersections ==========
def subseqOfSelf[T](@induct s: List[T]): Unit = {
}.ensuring(ListSpecs.subseq(s, s))

def withoutLastIsSubseq(@induct s: List[Position]): Unit = {
}.ensuring(ListSpecs.subseq(withoutLast(s), s))

def withoutLastNoSelfIntersection(s: List[Position]): Unit = {
  require(noSelfIntersection(s))
  withoutLastIsSubseq(s)
  ListSpecs.noDuplicateSubseq(withoutLast(s), s)
}.ensuring(noSelfIntersection(withoutLast(s)))
