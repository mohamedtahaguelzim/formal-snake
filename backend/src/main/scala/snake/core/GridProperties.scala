package snake.core

import snake.core.Grid._
import snake.core.ListProperties._
import snake.core.Properties._

import stainless.lang._
import stainless.math._
import stainless.collection._
import stainless.annotation._

object GridProperties:
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

  def rangeNoDuplicate(right: BigInt, left: BigInt): Unit = {
    require(0 <= left && left < right)
    decreases(right - left)
    val tail = if left < right - 1 then range(right, left + 1) else Nil()
    if left < right - 1 then
      rangeNoDuplicate(right, left + 1)
      if tail.contains(left) then rangeLowerBound(right, left + 1, left) else ()
    else ()
  }.ensuring(ListSpecs.noDuplicate(range(right, left)))

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
  }.ensuring(
    ListSpecs.noDuplicate(range.map(i => Position(i % width, i / width)))
  )

  def gridNoDuplicate(width: BigInt, height: BigInt): Unit = {
    require(width > 0 && height > 0)
    rangeNoDuplicate(width * height, 0)
    gridInjectionNoDuplicate(range(width * height), width)
  }.ensuring(ListSpecs.noDuplicate(grid(width, height)))


  def gridWithoutSnakeNonEmpty(state: GameState): Unit = {
    require(validPlayingState(state))
    gridNoDuplicate(state.gridWidth, state.gridHeight)
    noDuplicateFilterLength(state.snake, grid(state.gridWidth, state.gridHeight))
  }.ensuring((grid(state.gridWidth, state.gridHeight) -- state.snake).nonEmpty)
