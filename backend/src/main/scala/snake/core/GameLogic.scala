package snake.core

import stainless.lang._
import stainless.math._
import stainless.collection._
import stainless.annotation._

object GameLogic:

  def range(size: BigInt, i: BigInt = 0): List[BigInt] = {
    require(0 <= i && i < size)
    decreases(size - i)
    i :: (if i < size - 1 then range(size, i + 1) else Nil())
  }.ensuring(_.length == size - i)

  def grid(width: BigInt, height: BigInt): List[Position] = {
    require(width > 0 && height > 0)
    range(width * height).map(i => Position(i % width, i / width))
  }.ensuring(_.length == width * height)

  def generateFood(state: GameState, seed: BigInt): Position = {
    require(validPlayingState(state))

    val emptyPositions = grid(state.gridWidth, state.gridHeight) -- state.snake
    gridWithoutSnakeNonEmpty(state)
    val index = abs(seed) % emptyPositions.length

    emptyPositions(index)
  }.ensuring(!state.snake.contains(_))

  def initializeGame(state: GameState, foodSeed: BigInt): GameState = {
    val withoutFood =
      state.copy(
        status = GameStatus.Playing,
        snake = List(state.initialSnakePosition),
        stateNumber = state.stateNumber + 1
      )
    withoutFood.copy(
      food = Some(generateFood(withoutFood, foodSeed))
    )
  }.ensuring(validPlayingState(_))

  def resetGame(config: GameConfig): GameState = {
    GameState(
      snake = Nil(),
      food = None(),
      direction = Direction.Right,
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

  def withoutLast(s: List[Position]): List[Position] = s match
    case Cons(h, t @ Cons(_, _)) => Cons(h, withoutLast(t))
    case _                       => Nil()

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
      val hasWon = newSnake.length == currState.gridWidth * currState.gridHeight

      withoutLastLength(currState.snake)
      assert(newSnake.length <= currState.snake.length + 1)

      withoutLastContinuous(currState.snake)

      withoutLastWithinBounds(currState.snake, currState.gridWidth, currState.gridHeight)

      withoutLastIsSubseq(currState.snake)
      ListSpecs.subseqNotContains(withoutLast(currState.snake), currState.snake, newHead)
      withoutLastNoSelfIntersection(currState.snake)

      if hasWon then
        currState.copy(
          snake = newSnake,
          food = None(),
          status = GameStatus.GameWon,
          stateNumber = currState.stateNumber + 1
        )
      else
        currState.copy(
          snake = newSnake,
          food =
            if !ateFood then currState.food
            else Some(generateFood(currState.copy(snake = newSnake), foodSeed)),
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
