package snake.core

import stainless.lang._
import stainless.collection._
import stainless.annotation._

object GameLogic:
  @extern // because of until...
  def generateFood(state: GameState, seed: BigInt): Position = {
    val emptyPositions = (
      for
        x <- (BigInt(0) until state.gridWidth)
        y <- (BigInt(0) until state.gridWidth)
        pos = Position(x, y)
        if !state.snake.contains(pos)
      yield pos
    ).toList

    if emptyPositions.isEmpty then Position(0, 0) // this should never happen!
    else emptyPositions((seed.abs % emptyPositions.length).toInt)
  }

  def createInitialSnake(
      headPos: Position,
      size: BigInt,
      direction: Direction
  ): List[Position] = {
    if size <= 0 then Nil()
    else headPos :: createInitialSnake(headPos - direction, size - 1, direction)
  }.ensuring(res =>
    continuous(res) &&
      (size == 0 ==> res.isEmpty) &&
      (size > 0 ==> (res.length == size))
  )

  def initializeGame(state: GameState, foodSeed: BigInt): GameState = {
    val initialSnake = createInitialSnake(
      state.initialSnakePosition,
      state.config.snakeStartSize,
      state.direction
    )
    state.copy(
      status = GameStatus.Playing,
      snake = initialSnake,
      food = Some(generateFood(state.copy(snake = initialSnake), foodSeed)),
      stateNumber = state.stateNumber + 1
    )
  }

  def resetGame(config: GameConfig): GameState = {
    GameState(
      snake = Nil(),
      food = None(),
      direction = Direction.Right,
      score = 0,
      status = GameStatus.Waiting,
      config = config,
      stateNumber = 0,
      pendingDirection = None()
    )
  }

  def isOppositeDirection(current: Direction, newDir: Direction): Boolean = {
    (current, newDir) match
      case (Direction.Up, Direction.Down)    => true
      case (Direction.Down, Direction.Up)    => true
      case (Direction.Left, Direction.Right) => true
      case (Direction.Right, Direction.Left) => true
      case _                                 => false
  }

  def queueDirectionChange(
      state: GameState,
      newDirection: Direction
  ): GameState = {
    val checkDirection = state.pendingDirection.getOrElse(state.direction)
    val canChangeDirection =
      state.snake.length == 1 || !isOppositeDirection(
        checkDirection,
        newDirection
      )
    if !canChangeDirection || checkDirection == newDirection then state
    else
      state.copy(
        pendingDirection = Some(newDirection),
        stateNumber = state.stateNumber + 1
      )
  }

  def processGameTick(state: GameState, foodSeed: BigInt): GameState = {
    require(validPlayingState(state))
    val currState = state.pendingDirection match
      case Some(dir) => state.copy(direction = dir, pendingDirection = None())
      case None()    => state

    val newHead = currState.nextHeadPosition
    if state.hasCollision(newHead) then
      currState.copy(
        status = GameStatus.GameOver,
        stateNumber = currState.stateNumber + 1
      )
    else
      val ateFood = currState.food == Some(newHead)
      val newTail =
        (if ateFood then currState.snake
         else withoutLast(currState.snake))
      val newSnake = newHead :: newTail
      val newScore = currState.score + (if ateFood then 10 else 0)
      val hasWon = newSnake.length == currState.gridWidth * currState.gridHeight

      withoutLastContinuous(currState.snake)
      withoutLastWithinBounds(currState.snake, currState.gridWidth, currState.gridHeight)

      if hasWon then
        currState.copy(
          snake = newSnake,
          food = None(),
          status = GameStatus.GameWon,
          score = newScore,
          stateNumber = currState.stateNumber + 1
        )
      else
        currState.copy(
          snake = newSnake,
          food =
            if !ateFood then currState.food
            else Some(generateFood(currState.copy(snake = newSnake), foodSeed)),
          score = newScore,
          stateNumber = currState.stateNumber + 1
        )
  }.ensuring(res => res.status == GameStatus.Playing ==> validPlayingState(res))

  def transition(
      state: GameState,
      input: GameInput,
      tickSeed: BigInt = 0,
      foodSeed: BigInt = 0
  ): GameState =
    (state.status, input) match
      case (GameStatus.Waiting, GameInput(_, true, _, _)) =>
        initializeGame(state, foodSeed)

      case (GameStatus.Playing, GameInput(Some(dir), _, _, _)) =>
        queueDirectionChange(state, dir)

      case (GameStatus.Playing, GameInput(_, _, true, _)) =>
        resetGame(state.config)

      case (GameStatus.Playing, GameInput(_, _, _, true)) =>
        resetGame(state.config)

      case (GameStatus.GameOver, GameInput(_, _, true, _)) =>
        resetGame(state.config)

      case (GameStatus.GameWon, GameInput(_, _, true, _)) =>
        resetGame(state.config)

      case (_, GameInput(_, _, _, true)) =>
        resetGame(state.config)

      case _ =>
        state

  def tickGame(state: GameState, foodSeed: BigInt): GameState =
    if validPlayingState(state) then processGameTick(state, foodSeed)
    else state
