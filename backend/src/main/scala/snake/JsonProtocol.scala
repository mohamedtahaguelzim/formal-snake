package snake

import spray.json.*

object JsonProtocol extends DefaultJsonProtocol:
  
  implicit val positionFormat: RootJsonFormat[Position] = jsonFormat2(Position.apply)
  
  implicit val directionFormat: RootJsonFormat[Direction] = new RootJsonFormat[Direction]:
    def write(direction: Direction): JsValue = JsString(direction.toString.toLowerCase)
    def read(json: JsValue): Direction = json match
      case JsString("up") => Direction.Up
      case JsString("down") => Direction.Down
      case JsString("left") => Direction.Left  
      case JsString("right") => Direction.Right
      case _ => throw DeserializationException("Expected direction string")

  implicit val gameStatusFormat: RootJsonFormat[GameStatus] = new RootJsonFormat[GameStatus]:
    def write(status: GameStatus): JsValue = JsString(status.toString.toLowerCase)
    def read(json: JsValue): GameStatus = json match
      case JsString("waiting") => GameStatus.Waiting
      case JsString("playing") => GameStatus.Playing
      case JsString("gameover") => GameStatus.GameOver
      case _ => throw DeserializationException("Expected game status string")

  implicit val gameStateFormat: RootJsonFormat[GameState] = jsonFormat9(GameState.apply)
  
  case class KeyPressMessage(key: String)
  implicit val keyPressFormat: RootJsonFormat[KeyPressMessage] = jsonFormat1(KeyPressMessage.apply)
  
  case class GameStateResponse(
    snake: List[Position],
    food: Option[Position], 
    score: Int,
    gameOver: Boolean,
    gridWidth: Int,
    gridHeight: Int,
    gameStarted: Boolean,
    connected: Boolean = true
  )
  
  implicit val gameStateResponseFormat: RootJsonFormat[GameStateResponse] = jsonFormat8(GameStateResponse.apply)
