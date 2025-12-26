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
  /** Generate a new food position inside the game's grid that does not collide with the current
    * snake.
    *
    * @param state the current game state that must satisfy `validPlayingState`.
    * @param seed a random value used to pick an index among empty cells.
    * @return a `Position` within bounds and not contained in `state.snake`.
    * @ensures the returned position is within the grid and is not on the snake.
    */
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

  /** Initialize the game to a `Playing` state. The snake of length 1 is placed in the center of the
    * grid, and the food is generated using the provided seed.
    *
    * @param state the current game state (typically `Waiting`).
    * @param foodSeed seed used to generate the food position.
    * @return new `GameState` in `Playing` status with a single-cell snake and food.
    * @ensuring the returned state is a `validPlayingState`.
    */
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
  }.ensuring(validPlayingState)

  /** Create a fresh `GameState` from a `GameConfig`.
    * The game is set to `Waiting` with an empty snake and no food.
    *
    * @param config the game configuration (grid size and speed).
    * @return a reset `GameState` ready to be initialized.
    */
  def resetGame(config: GameConfig) = GameState(config = config)

  /** Queue a requested direction change for the next tick. The change is validated so the snake
    * cannot reverse into itself.
    *
    * @param state the current `GameState`.
    * @param newDirection the requested `Direction`.
    * @return an updated `GameState` with `pendingDirection` set if the change is valid, otherwise
    *         the original state.
    */
  def queueDirectionChange(
      state: GameState,
      newDirection: Direction
  ): GameState =
    val checkDirection = state.pendingDirection.getOrElse(state.direction)
    val canChangeDirection =
      state.snake.length == 1 || checkDirection != newDirection.opposite
    if !canChangeDirection || checkDirection == newDirection then state
    else state.copy(pendingDirection = Some(newDirection))

  def updateDirection(state: GameState): GameState =
    state.pendingDirection match
      case Some(dir) => state.copy(direction = dir, pendingDirection = None())
      case None()    => state

  /** Advance the game by one tick while in a `Playing` state. This computes the next head position,
    * checks for collisions, moves the snake (growing if food was eaten), handles wins, and may
    * generate new food.
    *
    * @param state a `validPlayingState`.
    * @param foodSeed seed used to generate new food if the snake eats.
    * @return the next `GameState` after applying one tick of game logic.
    * @ensuring the returned state is a valid transition from `state` and, if still `Playing`,
    *           satisfies `validPlayingState`
    */
  def processGameTick(state: GameState, foodSeed: BigInt): GameState = {
    require(validPlayingState(state))
    val currState = updateDirection(state)

    subseqOfSelf(currState.snake)
    ListSpecs.subseqTail(currState.snake, currState.snake)

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

      withoutLastContiguous(currState.snake)

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
