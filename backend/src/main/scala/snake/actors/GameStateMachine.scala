package snake.actors

import snake.core.{GameState, GameLogic, GameInput, GameConfig, Direction, GameStatus}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import scala.concurrent.duration.*

import stainless.lang._
import stainless.collection._
import stainless.annotation._

object GameStateMachine:
  
  sealed trait Command
  case object StartGame extends Command
  case object StopGame extends Command
  case object Tick extends Command
  case class ChangeDirection(direction: Direction) extends Command
  case object ResetGame extends Command
  case class GetState(replyTo: ActorRef[GameState]) extends Command
  case class SetGameConfig(gridWidth: BigInt, gridHeight: BigInt, gameSpeed: BigInt) extends Command

  def apply(): Behavior[Command] = waiting(GameState())

  private def waiting(state: GameState): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case StartGame =>
          val newState = GameLogic.initializeGame(state, BigInt(scala.util.Random.nextInt()))
          context.log.info("Game started!")
          scheduleNextTick(context, newState)
          playing(newState)
          
        case SetGameConfig(width, height, speed) =>
          context.log.info(s"Game configured: ${width}x${height}, speed: ${speed}ms")
          val newConfig = GameConfig(width, height, speed)
          waiting(state.copy(config = newConfig, stateNumber = state.stateNumber + 1))
          
        case GetState(replyTo) =>
          replyTo ! state
          Behaviors.same
          
        case _ =>
          context.log.warn(s"Ignoring message $message in waiting state")
          Behaviors.same
    }

  private def processTick(state: GameState, context: ActorContext[Command]): Behavior[Command] =
    val newState = GameLogic.processGameTick(state, BigInt(scala.util.Random.nextInt()))
    if newState.status == GameStatus.GameOver || newState.status == GameStatus.GameWon then
      val finalScore = ((newState.snake.length - 1) * 10).toInt
      context.log.info(s"Game Over! Final score: ${finalScore}")
      gameOver(newState)
    else
      scheduleNextTick(context, newState)
      playing(newState)

  private def playing(state: GameState): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case Tick =>
          processTick(state, context)

        case ChangeDirection(newDirection) =>
          val input = GameInput(direction = Some(newDirection))
          val updatedState = GameLogic.transition(state, input)
          
          // If turn-based mode (gameSpeed = 0), process tick immediately on input
          if state.config.gameSpeed == 0 && updatedState.pendingDirection.isDefined then
            processTick(updatedState, context)
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
          
        case SetGameConfig(width, height, speed) =>
          context.log.info(s"Game configured from game over: ${width}x${height}, speed: ${speed}ms")
          val newConfig = GameConfig(width, height, speed)
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
      context.scheduleOnce(state.config.gameSpeed.longValue.millis, context.self, Tick)
