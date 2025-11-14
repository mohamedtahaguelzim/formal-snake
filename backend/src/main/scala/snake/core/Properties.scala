package snake.core

// Properties to be verified with Stainless
object Properties:
  
  // Property: Snake continuity
  // The snake is continuous, that is, the i-th position of the snake is given by the
  // i+1-th position plus the i+1-th direction for 0 ≤ i < snake length - 1
  def snakeIsContinuous(snake: List[Position]): Boolean =
    snake.sliding(2).forall { case List(current, next) =>
      val dx = math.abs(current.x - next.x)
      val dy = math.abs(current.y - next.y)
      (dx == 1 && dy == 0) || (dx == 0 && dy == 1)
    case _ => true
    }

  // Property: No self collision
  def noSelfCollision(snake: List[Position]): Boolean =
    snake.distinct.length == snake.length

  // Property: Snake within bounds
  def snakeWithinBounds(snake: List[Position], gridWidth: Int, gridHeight: Int): Boolean =
    snake.forall { pos =>
      pos.x >= 0 && pos.x < gridWidth && pos.y >= 0 && pos.y < gridHeight
    }

  // Property: Food not on snake
  def foodNotOnSnake(snake: List[Position], food: Option[Position]): Boolean =
    food match
      case Some(pos) => !snake.contains(pos)
      case None => true

  // Property: Valid state
  def isValidState(state: GameState): Boolean =
    state.snake.nonEmpty ==> (
      snakeIsContinuous(state.snake) &&
      noSelfCollision(state.snake) &&
      snakeWithinBounds(state.snake, state.gridWidth, state.gridHeight) &&
      foodNotOnSnake(state.snake, state.food)
    )

  // Property: State transition preserves invariants
  def transitionPreservesInvariants(oldState: GameState, newState: GameState): Boolean =
    (isValidState(oldState) && newState.status != GameStatus.GameOver) ==> isValidState(newState)

  // Property: Score only increases
  def scoreOnlyIncreases(oldState: GameState, newState: GameState): Boolean =
    newState.score >= oldState.score

  // Property: State number increases
  def stateNumberIncreases(oldState: GameState, newState: GameState): Boolean =
    newState.stateNumber >= oldState.stateNumber

  // Helper function for logical implication
  extension (b1: Boolean) def ==>(b2: Boolean): Boolean = !b1 || b2