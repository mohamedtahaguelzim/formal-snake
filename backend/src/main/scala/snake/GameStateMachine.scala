package snake

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import scala.concurrent.duration.*

object GameStateMachine:
  
  sealed trait Command
  case object StartGame extends Command
  case object Tick extends Command
  case class ChangeDirection(direction: Direction) extends Command
  case object ResetGame extends Command
  case class GetState(replyTo: ActorRef[GameState]) extends Command

  def apply(): Behavior[Command] = waiting(GameState())

  private def waiting(state: GameState): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case StartGame =>
          val newState = state.copy(
            status = GameStatus.Playing,
            gameStarted = true,
            food = Some(state.generateFood)
          )
          context.log.info("Game started!")
          scheduleNextTick(context)
          playing(newState)
          
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
            gameOver(newState.copy(gameOver = true))
          else
            scheduleNextTick(context)
            playing(newState)
            
        case ChangeDirection(newDirection) =>
          val updatedState = changeDirection(state, newDirection)
          playing(updatedState)
          
        case GetState(replyTo) =>
          replyTo ! state
          Behaviors.same
          
        case ResetGame =>
          context.log.info("Resetting game")
          waiting(GameState())
          
        case _ =>
          context.log.warn(s"Ignoring message $message in playing state")
          Behaviors.same
    }

  private def gameOver(state: GameState): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case ResetGame =>
          context.log.info("Restarting game from game over")
          waiting(GameState())
          
        case GetState(replyTo) =>
          replyTo ! state
          Behaviors.same
          
        case _ =>
          context.log.warn(s"Ignoring message $message in game over state")
          Behaviors.same
    }

  private def processGameTick(state: GameState): GameState =
    val newHead = state.nextHeadPosition
    
    if state.hasCollision(newHead) then
      state.copy(status = GameStatus.GameOver)
    else
      val ateFood = state.food.contains(newHead)
      val newSnake = if ateFood then
        newHead :: state.snake
      else
        newHead :: state.snake.dropRight(1)
        
      val newFood = if ateFood then
        Some(state.generateFood)
      else
        state.food
        
      val newScore = if ateFood then state.score + 10 else state.score
      
      state.copy(
        snake = newSnake,
        food = newFood,
        score = newScore
      )

  private def changeDirection(state: GameState, newDirection: Direction): GameState =
    val currentDirection = state.direction
    val isOpposite = (currentDirection, newDirection) match
      case (Direction.Up, Direction.Down) => true
      case (Direction.Down, Direction.Up) => true
      case (Direction.Left, Direction.Right) => true
      case (Direction.Right, Direction.Left) => true
      case _ => false
      
    if isOpposite then state else state.copy(direction = newDirection)

  private def scheduleNextTick(context: ActorContext[Command]): Unit =
    context.scheduleOnce(200.millis, context.self, Tick)
