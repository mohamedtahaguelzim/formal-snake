package snake

case class Position(x: Int, y: Int)

enum Direction:
  case Up, Down, Left, Right

enum GameStatus:
  case Waiting, Playing, GameOver

case class GameState(
  snake: List[Position] = List(Position(10, 10)),
  food: Option[Position] = None,
  direction: Direction = Direction.Right,
  score: Int = 0,
  status: GameStatus = GameStatus.Waiting,
  gridWidth: Int = 20,
  gridHeight: Int = 20,
  gameStarted: Boolean = false,
  gameOver: Boolean = false
):
  def isValidPosition(pos: Position): Boolean =
    pos.x >= 0 && pos.x < gridWidth && pos.y >= 0 && pos.y < gridHeight

  def hasCollision(newHead: Position): Boolean =
    !isValidPosition(newHead) || snake.contains(newHead)

  def nextHeadPosition: Position =
    val head = snake.head
    direction match
      case Direction.Up => Position(head.x, head.y - 1)
      case Direction.Down => Position(head.x, head.y + 1)
      case Direction.Left => Position(head.x - 1, head.y)
      case Direction.Right => Position(head.x + 1, head.y)

  def generateFood: Position =
    val random = scala.util.Random
    var newFood = Position(random.nextInt(gridWidth), random.nextInt(gridHeight))
    while snake.contains(newFood) do
      newFood = Position(random.nextInt(gridWidth), random.nextInt(gridHeight))
    newFood
