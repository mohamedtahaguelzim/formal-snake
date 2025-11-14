package snake.actors

import snake.core.{GameState, GameLogic, GameInput, GameConfig, Direction, GameStatus}
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
  case class SetGameConfig(gridWidth: Int, gridHeight: Int, gameSpeed: Int, snakeStartSize: Int = 1) extends Command

  def apply(): Behavior[Command] = waiting(GameState())

  private def waiting(state: GameState): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case StartGame =>
          val newState = GameLogic.initializeGame(state, scala.util.Random.nextInt())
          context.log.info("Game started!")
          scheduleNextTick(context, newState)
          playing(newState)
          
        case SetGameConfig(width, height, speed, startSize) =>
          context.log.info(s"Game configured: ${width}x${height}, speed: ${speed}ms, start size: ${startSize}")
          val newConfig = GameConfig(width, height, speed, startSize)
          waiting(state.copy(config = newConfig, stateNumber = state.stateNumber + 1))
          
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
          val newState = GameLogic.tickGame(state, scala.util.Random.nextInt())
          if newState.status == GameStatus.GameOver then
            context.log.info(s"Game Over! Final score: ${newState.score}")
            gameOver(newState)
          else
            scheduleNextTick(context, newState)
            playing(newState)
            
        case ChangeDirection(newDirection) =>
          val input = GameInput(direction = Some(newDirection))
          val updatedState = GameLogic.transition(state, input)
          
          // If turn-based mode (gameSpeed = 0), process tick immediately on input
          if state.config.gameSpeed == 0 then
            val newState = GameLogic.tickGame(updatedState, scala.util.Random.nextInt())
            if newState.status == GameStatus.GameOver then
              context.log.info(s"Game Over! Final score: ${newState.score}")
              gameOver(newState)
            else
              playing(newState)
          else
            playing(updatedState)
          
        case GetState(replyTo) =>
          replyTo ! state
          Behaviors.same
          
        case StopGame =>
          context.log.info("Game stopped by user")
          val input = GameInput(stopGame = true)
          waiting(GameLogic.transition(state, input))
          
        case ResetGame =>
          context.log.info("Resetting game")
          val input = GameInput(resetGame = true)
          waiting(GameLogic.transition(state, input))
          
        case _ =>
          context.log.warn(s"Ignoring message $message in playing state")
          Behaviors.same
    }

  private def gameOver(state: GameState): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case ResetGame =>
          context.log.info("Restarting game from game over")
          waiting(GameLogic.resetGame(state.config))
          
        case StopGame =>
          context.log.info("Stopping game from game over")
          waiting(GameLogic.resetGame(state.config))
          
        case SetGameConfig(width, height, speed, startSize) =>
          context.log.info(s"Game configured from game over: ${width}x${height}, speed: ${speed}ms, start size: ${startSize}")
          val newConfig = GameConfig(width, height, speed, startSize)
          waiting(GameLogic.resetGame(newConfig))
          
        case GetState(replyTo) =>
          replyTo ! state
          Behaviors.same
          
        case _ =>
          context.log.warn(s"Ignoring message $message in game over state")
          Behaviors.same
    }

  private def scheduleNextTick(context: ActorContext[Command], state: GameState): Unit =
    // Only schedule automatic ticks if gameSpeed > 0
    // When gameSpeed = 0, game is turn-based and only advances on player input
    if state.config.gameSpeed > 0 then
      context.scheduleOnce(state.config.gameSpeed.millis, context.self, Tick)
