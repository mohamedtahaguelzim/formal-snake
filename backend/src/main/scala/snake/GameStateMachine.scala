package snake

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import scala.concurrent.duration.*

object GameStateMachine:
  
  sealed trait Command
  case object StartGame extends Command
  case object StopGame extends Command
  case object Tick extends Command
  case class ChangeDirection(direction: Direction) extends Command
  case object ResetGame extends Command
  case class GetState(replyTo: ActorRef[GameState]) extends Command
  case class SetGameConfig(gridWidth: Int, gridHeight: Int, gameSpeed: Int) extends Command

  def apply(): Behavior[Command] = waiting(GameState())

  private def waiting(state: GameState): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case StartGame =>
          val initialPos = state.getInitialSnakePosition
          val newState = state.copy(
            status = GameStatus.Playing,
            gameStarted = true,
            snake = List(initialPos),
            food = Some(state.generateFood),
            stateNumber = state.stateNumber + 1
          )
          context.log.info("Game started!")
          scheduleNextTick(context, newState)
          playing(newState)
          
        case SetGameConfig(width, height, speed) =>
          context.log.info(s"Game configured: ${width}x${height}, speed: ${speed}ms")
          waiting(state.copy(gridWidth = width, gridHeight = height, gameSpeed = speed, stateNumber = state.stateNumber + 1))
          
        case GetState(replyTo) =>
          replyTo ! state
          Behaviors.same
          
        case _ =>
          context.log.warn(s"Ignoring message $message in waiting state")
          Behaviors.same
    }

  private def playing(state: GameState): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case Tick =>
          val newState = processGameTick(state)
          if newState.status == GameStatus.GameOver then
            context.log.info(s"Game Over! Final score: ${newState.score}")
            gameOver(newState.copy(gameOver = true, stateNumber = newState.stateNumber + 1))
          else
            scheduleNextTick(context, newState)
            playing(newState)
            
        case ChangeDirection(newDirection) =>
          // Queue the direction change
          val updatedState = queueDirectionChange(state, newDirection)
          
          // If turn-based mode (gameSpeed = 0), process tick immediately on input
          if state.gameSpeed == 0 then
            val newState = processGameTick(updatedState)
            if newState.status == GameStatus.GameOver then
              context.log.info(s"Game Over! Final score: ${newState.score}")
              gameOver(newState.copy(gameOver = true, stateNumber = newState.stateNumber + 1))
            else
              playing(newState)
          else
            playing(updatedState)
          
        case GetState(replyTo) =>
          replyTo ! state
          Behaviors.same
          
        case StopGame =>
          context.log.info("Game stopped by user")
          waiting(state.copy(
            snake = List.empty,
            food = None,
            direction = Direction.Right,
            score = 0,
            status = GameStatus.Waiting,
            gameStarted = false,
            gameOver = false,
            stateNumber = state.stateNumber + 1,
            pendingDirection = None
          ))
          
        case ResetGame =>
          context.log.info("Resetting game")
          waiting(state.copy(
            snake = List.empty,
            food = None,
            direction = Direction.Right,
            score = 0,
            status = GameStatus.Waiting,
            gameStarted = false,
            gameOver = false,
            gameWon = false,
            stateNumber = state.stateNumber + 1,
            pendingDirection = None
          ))
          
        case _ =>
          context.log.warn(s"Ignoring message $message in playing state")
          Behaviors.same
    }

  private def gameOver(state: GameState): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case ResetGame =>
          context.log.info("Restarting game from game over")
          waiting(GameState(gridWidth = state.gridWidth, gridHeight = state.gridHeight, gameSpeed = state.gameSpeed, stateNumber = state.stateNumber + 1))
          
        case GetState(replyTo) =>
          replyTo ! state
          Behaviors.same
          
        case _ =>
          context.log.warn(s"Ignoring message $message in game over state")
          Behaviors.same
    }

  private def processGameTick(state: GameState): GameState =
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
          gameWon = true,
          stateNumber = currentState.stateNumber + 1
        )
      else
        val newScore = if ateFood then currentState.score + 10 else currentState.score
        
        // Generate new food with the UPDATED snake position
        val stateWithNewSnake = currentState.copy(snake = newSnake)
        val newFood = if ateFood then
          Some(stateWithNewSnake.generateFood)
        else
          currentState.food
        
        stateWithNewSnake.copy(
          food = newFood,
          score = newScore,
          stateNumber = currentState.stateNumber + 1
        )

  private def queueDirectionChange(state: GameState, newDirection: Direction): GameState =
    // Check against current direction (or pending if exists)
    val checkDirection = state.pendingDirection.getOrElse(state.direction)
    val isOpposite = (checkDirection, newDirection) match
      case (Direction.Up, Direction.Down) => true
      case (Direction.Down, Direction.Up) => true
      case (Direction.Left, Direction.Right) => true
      case (Direction.Right, Direction.Left) => true
      case _ => false
      
    if isOpposite || checkDirection == newDirection then 
      state 
    else 
      state.copy(pendingDirection = Some(newDirection), stateNumber = state.stateNumber + 1)

  private def scheduleNextTick(context: ActorContext[Command], state: GameState): Unit =
    // Only schedule automatic ticks if gameSpeed > 0
    // When gameSpeed = 0, game is turn-based and only advances on player input
    if state.gameSpeed > 0 then
      context.scheduleOnce(state.gameSpeed.millis, context.self, Tick)
