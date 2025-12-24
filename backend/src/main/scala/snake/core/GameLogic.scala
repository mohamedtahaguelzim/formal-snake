package snake.core

import snake.core.Grid._
import snake.core.GridProperties._
import snake.core.ListProperties._
import snake.core.ListUtils._
import snake.core.ListUtilsProperties._
import snake.core.Properties._

import stainless.lang._
import stainless.math._
import stainless.collection._

object GameLogic:
  def generateFood(state: GameState, seed: BigInt): Position = {
    require(validPlayingState(state))

    val fullGrid = grid(state.gridWidth, state.gridHeight)
    val emptyPositions = fullGrid -- state.snake

    removeSubseq(state.snake, fullGrid)
    subseqPreservesProperty(
      emptyPositions,
      fullGrid,
      isWithinBounds(state.gridWidth, state.gridHeight)
    )
    gridWithoutSnakeNonEmpty(state)

    val index = abs(seed) % emptyPositions.length
    ListSpecs.applyForAll(
      emptyPositions,
      index,
      isWithinBounds(state.gridWidth, state.gridHeight)
    )

    emptyPositions(index)
  }.ensuring(!state.hasCollision(_))

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

  def resetGame(config: GameConfig): GameState =
    GameState(
      snake = Nil(),
      food = None(),
      direction = Direction.Right,
      status = GameStatus.Waiting,
      config = config,
      stateNumber = 0,
      pendingDirection = None()
    )

  def queueDirectionChange(
      state: GameState,
      newDirection: Direction
  ): GameState =
    val checkDirection = state.pendingDirection.getOrElse(state.direction)
    val canChangeDirection =
      state.snake.length == 1 || checkDirection != newDirection.opposite
    if !canChangeDirection || checkDirection == newDirection then state
    else
      state.copy(
        pendingDirection = Some(newDirection),
        stateNumber = state.stateNumber + 1
      )

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

      subseqOfSelf(currState.snake)
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
  }.ensuring(next =>
    validTransistion(state, next) &&
      next.status == GameStatus.Playing ==> validPlayingState(next)
  )

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
