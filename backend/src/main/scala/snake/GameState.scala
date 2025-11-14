package snake

case class Position(x: Int, y: Int)

enum Direction:
  case Up, Down, Left, Right

enum GameStatus:
  case Waiting, Playing, GameOver, GameWon

case class GameState(
  snake: List[Position] = List.empty,
  food: Option[Position] = None,
  direction: Direction = Direction.Right,
  score: Int = 0,
  status: GameStatus = GameStatus.Waiting,
  gridWidth: Int = 20,
  gridHeight: Int = 20,
  gameStarted: Boolean = false,
  gameOver: Boolean = false,
  gameWon: Boolean = false,
  stateNumber: Long = 0,
  gameSpeed: Int = 200,
  pendingDirection: Option[Direction] = None
):
  def initialSnakePosition: Position = Position(gridWidth / 2, gridHeight / 2)
  
  def withInitialSnake: GameState = 
    if (snake.isEmpty) copy(snake = List(initialSnakePosition)) else this
  def isValidPosition(pos: Position): Boolean =
    pos.x >= 0 && pos.x < gridWidth && pos.y >= 0 && pos.y < gridHeight

  def hasCollision(newHead: Position): Boolean =
    !isValidPosition(newHead) || snake.contains(newHead)

  def nextHeadPosition: Position =
    if snake.isEmpty then getInitialSnakePosition
    else
      val head = snake.head
      direction match
        case Direction.Up => Position(head.x, head.y - 1)
        case Direction.Down => Position(head.x, head.y + 1)
        case Direction.Left => Position(head.x - 1, head.y)
        case Direction.Right => Position(head.x + 1, head.y)

  def generateFood: Position =
    // Find all empty positions
    val emptyPositions = (for {
      x <- 0 until gridWidth
      y <- 0 until gridHeight
      pos = Position(x, y)
      if !snake.contains(pos)
    } yield pos).toList
    
    // If no empty positions, return any position (game should be won)
    if emptyPositions.isEmpty then
      Position(0, 0)
    else
      val random = scala.util.Random
      emptyPositions(random.nextInt(emptyPositions.length))
  
  def getInitialSnakePosition: Position =
    Position(gridWidth / 2, gridHeight / 2)
