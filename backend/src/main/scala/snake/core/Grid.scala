package snake.core

import snake.core.GridProperties._

import stainless.lang._
import stainless.collection._

object Grid:
  def isWithinBounds(width: BigInt, height: BigInt) =
    (pos: Position) => 0 <= pos.x && pos.x < width && 0 <= pos.y && pos.y < height

  def range(right: BigInt, left: BigInt = 0): List[BigInt] = {
    require(left < right)
    decreases(right - left)
    left :: (if left < right - 1 then range(right, left + 1) else Nil())
  }.ensuring(res =>
    res.length == right - left &&
      res.forall(_ < right)
  )

  def grid(width: BigInt, height: BigInt): List[Position] = {
    require(width > 0 && height > 0)
    rangeMappingWithinBound(width * height, 0, width, height)
    range(width * height).map(i => Position(i % width, i / width))
  }.ensuring(res =>
    res.length == width * height &&
      res.forall(isWithinBounds(width, height))
  )
