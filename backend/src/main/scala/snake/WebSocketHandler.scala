package snake

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern.*
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import spray.json.*
import JsonProtocol.*

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class WebSocketHandler(gameActor: ActorRef[GameStateMachine.Command])
                      (implicit system: ActorSystem[_], ec: ExecutionContext):
  
  implicit val timeout: Timeout = 3.seconds
  
  @volatile private var lastStateNumber: Long = -1

  def gameFlow: Flow[Message, Message, Any] =
    Flow[Message]
      .mapAsync(1) {
        case TextMessage.Strict(text) =>
          handleMessage(text)
        case TextMessage.Streamed(stream) =>
          stream.runFold("")(_ + _).flatMap(handleMessage)
        case _ =>
          Future.successful(TextMessage.Strict("""{"error": "Unsupported message type"}"""))
      }
      .merge(gameStateUpdates)

  private def handleMessage(text: String): Future[TextMessage] =
    try {
      val json = text.parseJson.asJsObject
      
      // Check if it's a config message
      if (json.fields.contains("gridWidth")) {
        val config = json.convertTo[GameConfigMessage]
        gameActor ! GameStateMachine.SetGameConfig(config.gridWidth, config.gridHeight, config.gameSpeed)
        getCurrentGameState.map(TextMessage.Strict(_))
      } else {
        // Otherwise it's a key press
        val keyPress = json.convertTo[KeyPressMessage]
        handleKeyPress(keyPress.key).map(TextMessage.Strict(_))
      }
    } catch {
      case e: Exception =>
        system.log.error(s"Error parsing message: ${e.getMessage}")
        Future.successful(TextMessage.Strict("""{"error": "Invalid message format"}"""))
    }

  private def handleKeyPress(key: String): Future[String] =
    key match
      case "ArrowUp" =>
        gameActor ! GameStateMachine.ChangeDirection(Direction.Up)
        getCurrentGameState
      case "ArrowDown" => 
        gameActor ! GameStateMachine.ChangeDirection(Direction.Down)
        getCurrentGameState
      case "ArrowLeft" =>
        gameActor ! GameStateMachine.ChangeDirection(Direction.Left)
        getCurrentGameState  
      case "ArrowRight" =>
        gameActor ! GameStateMachine.ChangeDirection(Direction.Right)
        getCurrentGameState
      case " " => // Spacebar
        gameActor ! GameStateMachine.StartGame
        getCurrentGameState
      case "r" | "R" =>
        gameActor ! GameStateMachine.ResetGame
        getCurrentGameState
      case "q" | "Q" =>
        gameActor ! GameStateMachine.StopGame
        getCurrentGameState
      case _ =>
        getCurrentGameState

  private def getCurrentGameState: Future[String] =
    gameActor.ask(GameStateMachine.GetState.apply).map { state =>
      val response = GameStateResponse(
        snake = state.snake,
        food = state.food,
        score = state.score,
        gameOver = state.gameOver,
        gameWon = state.gameWon,
        gridWidth = state.gridWidth,
        gridHeight = state.gridHeight,
        gameStarted = state.gameStarted,
        stateNumber = state.stateNumber
      )
      response.toJson.toString
    }

  private def gameStateUpdates: Source[TextMessage, Any] =
    Source.tick(100.millis, 100.millis, ())
      .mapAsync(1) { _ =>
        gameActor.ask(GameStateMachine.GetState.apply).map { state =>
          if (state.stateNumber != lastStateNumber) {
            lastStateNumber = state.stateNumber
            val response = GameStateResponse(
              snake = state.snake,
              food = state.food,
              score = state.score,
              gameOver = state.gameOver,
              gameWon = state.gameWon,
              gridWidth = state.gridWidth,
              gridHeight = state.gridHeight,
              gameStarted = state.gameStarted,
              stateNumber = state.stateNumber
            )
            Some(response.toJson.toString)
          } else {
            None
          }
        }
      }
      .collect { case Some(state) => TextMessage.Strict(state) }
