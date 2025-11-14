package snake.core

case class Position(x: Int, y: Int)

enum Direction:
  case Up, Down, Left, Right

enum GameStatus:
  case Waiting, Playing, GameOver, GameWon

case class GameInput(
  direction: Option[Direction] = None,
  startGame: Boolean = false,
  resetGame: Boolean = false,
  stopGame: Boolean = false
)

case class GameConfig(
  gridWidth: Int = 20,
  gridHeight: Int = 20,
  gameSpeed: Int = 200,
  snakeStartSize: Int = 1
)

case class GameState(
  snake: List[Position] = List.empty,
  food: Option[Position] = None,
  direction: Direction = Direction.Right,
  score: Int = 0,
  status: GameStatus = GameStatus.Waiting,
  config: GameConfig = GameConfig(),
  stateNumber: Long = 0,
  pendingDirection: Option[Direction] = None
):
  def gridWidth: Int = config.gridWidth
  def gridHeight: Int = config.gridHeight
  
  def initialSnakePosition: Position = Position(gridWidth / 2, gridHeight / 2)
  
  def isValidPosition(pos: Position): Boolean =
    pos.x >= 0 && pos.x < gridWidth && pos.y >= 0 && pos.y < gridHeight

  def hasCollision(newHead: Position): Boolean =
    !isValidPosition(newHead) || snake.contains(newHead)

  def nextHeadPosition: Position =
    if snake.isEmpty then initialSnakePosition
    else
      val head = snake.head
      direction match
        case Direction.Up => Position(head.x, head.y - 1)
        case Direction.Down => Position(head.x, head.y + 1)
        case Direction.Left => Position(head.x - 1, head.y)
        case Direction.Right => Position(head.x + 1, head.y)