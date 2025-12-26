package snake.core

import snake.core.Grid._

import stainless.lang._
import stainless.collection._

enum Direction:
  case Up, Down, Left, Right

  def opposite: Direction =
    this match
      case Up    => Down
      case Down  => Up
      case Left  => Right
      case Right => Left

case class Position(x: BigInt, y: BigInt):
  def +(other: Direction) =
    other match
      case Direction.Up    => Position(x, y - 1)
      case Direction.Down  => Position(x, y + 1)
      case Direction.Left  => Position(x - 1, y)
      case Direction.Right => Position(x + 1, y)

  def -(other: Direction) =
    this + other.opposite

enum GameStatus:
  case Waiting, Playing, GameOver, GameWon

case class GameInput(
    direction: Option[Direction] = None(),
    startGame: Boolean = false,
    resetGame: Boolean = false,
    stopGame: Boolean = false
)

case class GameConfig(
    gridWidth: BigInt = 6,
    gridHeight: BigInt = 4,
    gameSpeed: BigInt = 200
):
  require(gridWidth >= 3 && gridHeight >= 3 && gameSpeed >= 0)

case class GameState(
    snake: List[Position] = Nil(),
    food: Option[Position] = None(),
    direction: Direction = Direction.Right,
    status: GameStatus = GameStatus.Waiting,
    config: GameConfig = GameConfig(),
    stateNumber: BigInt = 0,
    pendingDirection: Option[Direction] = None()
):
  def gridWidth: BigInt = config.gridWidth
  def gridHeight: BigInt = config.gridHeight

  def initialSnakePosition: Position = {
    Position(gridWidth / 2, gridHeight / 2)
  }.ensuring(isWithinBounds(gridWidth, gridHeight)(_))

  def hasCollision(pos: Position): Boolean =
    !isWithinBounds(gridWidth, gridHeight)(pos) || snake.contains(pos)

  def nextHeadPosition: Position =
    require(snake.nonEmpty)
    snake.head + direction
