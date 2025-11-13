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
      val keyPress = text.parseJson.convertTo[KeyPressMessage]
      handleKeyPress(keyPress.key).map(TextMessage.Strict(_))
    } catch {
      case _: Exception =>
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
      case _ =>
        getCurrentGameState

  private def getCurrentGameState: Future[String] =
    gameActor.ask(GameStateMachine.GetState.apply).map { state =>
      val response = GameStateResponse(
        snake = state.snake,
        food = state.food,
        score = state.score,
        gameOver = state.gameOver,
        gridWidth = state.gridWidth,
        gridHeight = state.gridHeight,
        gameStarted = state.gameStarted
      )
      response.toJson.toString
    }

  private def gameStateUpdates: Source[TextMessage, Any] =
    Source.tick(100.millis, 100.millis, ())
      .mapAsync(1)(_ => getCurrentGameState)
      .map(TextMessage.Strict(_))
