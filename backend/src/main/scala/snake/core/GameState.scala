package snake.core

import stainless.lang._
import stainless.collection._
import stainless.annotation._

enum Direction:
  case Up, Down, Left, Right

case class Position(x: BigInt, y: BigInt):
  def +(other: Direction) =
    other match
      case Direction.Up    => Position(x, y - 1)
      case Direction.Down  => Position(x, y + 1)
      case Direction.Left  => Position(x - 1, y)
      case Direction.Right => Position(x + 1, y)

  def -(other: Direction) =
    other match
      case Direction.Up    => Position(x, y + 1)
      case Direction.Down  => Position(x, y - 1)
      case Direction.Left  => Position(x + 1, y)
      case Direction.Right => Position(x - 1, y)

enum GameStatus:
  case Waiting, Playing, GameOver, GameWon

case class GameInput(
    direction: Option[Direction] = None(),
    startGame: Boolean = false,
    resetGame: Boolean = false,
    stopGame: Boolean = false
):
  require(true) // change to: (direction != None xor startGame xor ...)

case class GameConfig(
    gridWidth: BigInt = 20,
    gridHeight: BigInt = 20,
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
  }.ensuring(isValidPosition(_))

  def isValidPosition(pos: Position): Boolean =
    0 <= pos.x && pos.x < gridWidth && 0 <= pos.y && pos.y < gridHeight

  def hasCollision(newHead: Position): Boolean =
    !isValidPosition(newHead) || snake.contains(newHead)

  def nextHeadPosition: Position =
    if snake.isEmpty then initialSnakePosition
    else snake.head + direction
