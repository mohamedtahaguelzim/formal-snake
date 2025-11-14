package snake.core

object GameLogic:
  
  def generateFood(state: GameState, seed: Int): Position =
    // Find all empty positions
    val emptyPositions = (for {
      x <- 0 until state.gridWidth
      y <- 0 until state.gridHeight
      pos = Position(x, y)
      if !state.snake.contains(pos)
    } yield pos).toList
    
    // If no empty positions, return any position (game should be won)
    if emptyPositions.isEmpty then
      Position(0, 0)
    else
      // Use deterministic seeded selection for formal verification
      // Ensure positive index by using absolute value
      val index = math.abs(seed) % emptyPositions.length
      emptyPositions(index)

  def isOppositeDirection(current: Direction, newDir: Direction): Boolean =
    (current, newDir) match
      case (Direction.Up, Direction.Down) => true
      case (Direction.Down, Direction.Up) => true
      case (Direction.Left, Direction.Right) => true
      case (Direction.Right, Direction.Left) => true
      case _ => false

  def queueDirectionChange(state: GameState, newDirection: Direction): GameState =
    // Check against current direction (or pending if exists)
    val checkDirection = state.pendingDirection.getOrElse(state.direction)
    
    // Allow opposite directions for single cell snake (no body to collide with)
    val isOpposite = isOppositeDirection(checkDirection, newDirection)
    val canChangeDirection = if state.snake.length <= 1 then true else !isOpposite
    
    if !canChangeDirection || checkDirection == newDirection then 
      state 
    else 
      state.copy(pendingDirection = Some(newDirection), stateNumber = state.stateNumber + 1)

  def processGameTick(state: GameState, foodSeed: Int): GameState =
    // Apply pending direction if exists
    val currentState = state.pendingDirection match
      case Some(dir) => state.copy(direction = dir, pendingDirection = None)
      case None => state
    
    val newHead = currentState.nextHeadPosition
    val ateFood = currentState.food.contains(newHead)
    
    // Check collision against the snake body AFTER tail removal (if not eating)
    // This allows moving into the current tail position when not growing
    val collisionCheckSnake = if ateFood then
      currentState.snake  // Check against full snake when eating
    else
      currentState.snake.dropRight(1)  // Exclude tail when not eating
    
    val hasCollision = !currentState.isValidPosition(newHead) || collisionCheckSnake.contains(newHead)
    
    if hasCollision then
      currentState.copy(status = GameStatus.GameOver, stateNumber = currentState.stateNumber + 1)
    else
      val newSnake = if ateFood then
        newHead :: currentState.snake
      else
        newHead :: currentState.snake.dropRight(1)
      
      // Check for win condition: snake fills entire grid
      val maxSnakeLength = currentState.gridWidth * currentState.gridHeight
      val hasWon = newSnake.length >= maxSnakeLength
      
      if hasWon then
        currentState.copy(
          snake = newSnake,
          food = None,
          score = currentState.score + (if ateFood then 10 else 0),
          status = GameStatus.GameWon,
          stateNumber = currentState.stateNumber + 1
        )
      else
        val newScore = if ateFood then currentState.score + 10 else currentState.score
        
        // Generate new food with the UPDATED snake position
        val stateWithNewSnake = currentState.copy(snake = newSnake)
        val newFood = if ateFood then
          Some(generateFood(stateWithNewSnake, foodSeed))
        else
          currentState.food
        
        stateWithNewSnake.copy(
          food = newFood,
          score = newScore,
          stateNumber = currentState.stateNumber + 1
        )

  def initializeGame(state: GameState, foodSeed: Int): GameState =
    val initialPos = state.initialSnakePosition
    val initialSnake = createInitialSnake(initialPos, state.config.snakeStartSize, state.direction)
    state.copy(
      status = GameStatus.Playing,
      snake = initialSnake,
      food = Some(generateFood(state.copy(snake = initialSnake), foodSeed)),
      stateNumber = state.stateNumber + 1
    )

  def createInitialSnake(headPos: Position, size: Int, direction: Direction): List[Position] =
    if size <= 0 then List.empty
    else
      val positions = for i <- 0 until size yield
        direction match
          case Direction.Right => Position(headPos.x - i, headPos.y)
          case Direction.Left => Position(headPos.x + i, headPos.y)
          case Direction.Down => Position(headPos.x, headPos.y - i)
          case Direction.Up => Position(headPos.x, headPos.y + i)
      positions.toList

  def resetGame(config: GameConfig): GameState =
    GameState(
      snake = List.empty,
      food = None,
      direction = Direction.Right,
      score = 0,
      status = GameStatus.Waiting,
      config = config,
      stateNumber = 0,
      pendingDirection = None
    )

  def transition(state: GameState, input: GameInput, tickSeed: Int = 0, foodSeed: Int = 0): GameState =
    (state.status, input) match
      case (GameStatus.Waiting, GameInput(_, true, _, _)) =>
        initializeGame(state, foodSeed)
        
      case (GameStatus.Playing, GameInput(Some(dir), _, _, _)) =>
        queueDirectionChange(state, dir)
        
      case (GameStatus.Playing, GameInput(_, _, true, _)) =>
        resetGame(state.config)
        
      case (GameStatus.Playing, GameInput(_, _, _, true)) =>
        resetGame(state.config)
        
      case (GameStatus.GameOver | GameStatus.GameWon, GameInput(_, _, true, _)) =>
        resetGame(state.config)
        
      case (_, GameInput(_, _, _, true)) => // Back to menu from any state
        resetGame(state.config)
        
      case _ =>
        state

  def tickGame(state: GameState, foodSeed: Int): GameState =
    if state.status == GameStatus.Playing then
      processGameTick(state, foodSeed)
    else
      state