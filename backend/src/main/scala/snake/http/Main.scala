package snake.http

import snake.actors.GameStateMachine
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.headers.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main:
  
  def main(args: Array[String]): Unit =
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "snake-game-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val gameActor = system.systemActorOf(GameStateMachine(), "game-state-machine")
    val webSocketHandler = new WebSocketHandler(gameActor)

    val corsHeaders = List(
      `Access-Control-Allow-Origin`.*,
      `Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST, HttpMethods.OPTIONS),
      `Access-Control-Allow-Headers`("Content-Type", "Authorization")
    )

    val routes: Route =
      respondWithHeaders(corsHeaders) {
        concat(
          path("ws") {
            get {
              handleWebSocketMessages(webSocketHandler.gameFlow)
            }
          },
          path("health") {
            get {
              complete(HttpEntity(ContentTypes.`application/json`, """{"status": "healthy"}"""))
            }
          },
          options {
            complete(StatusCodes.OK)
          }
        )
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)
    
    bindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Snake Game Backend started at http://${address.getHostString}:${address.getPort}")
        system.log.info("WebSocket endpoint available at ws://localhost:8080/ws")
        
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint", ex)
        system.terminate()
    }
