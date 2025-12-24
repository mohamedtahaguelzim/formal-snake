package snake.core

import snake.core.GridProperties._

import stainless.lang._
import stainless.collection._

object Grid:
  def isWithinBounds(width: BigInt, height: BigInt) =
    (pos: Position) =>
      0 <= pos.x && pos.x < width && 0 <= pos.y && pos.y < height


  /** Builds a list of consecutive integers from `left` (inclusive) to `right` (exclusive).
    *
    * @ensures the length of the resulting list is `right - left` and every element of the list is
    *          strictly less than `right`
    */
  def range(right: BigInt, left: BigInt = 0): List[BigInt] = {
    require(left < right)
    decreases(right - left)
    left :: (if left < right - 1 then range(right, left + 1) else Nil())
  }.ensuring(res =>
    res.length == right - left &&
      res.forall(_ < right)
  )

  /** Builds all `Positions`s in the grid by mapping a `range`.
    *
    * @ensures the length of the resulting list is `width * height` and every position is within the
    *          grid bounds.
    */
  def grid(width: BigInt, height: BigInt): List[Position] = {
    require(width > 0 && height > 0)
    rangeMappingWithinBound(width * height, 0, width, height)
    range(width * height).map(i => Position(i % width, i / width))
  }.ensuring(res =>
    res.length == width * height &&
      res.forall(isWithinBounds(width, height))
  )
