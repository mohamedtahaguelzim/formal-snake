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
      case JsString("gamewon") => GameStatus.GameWon
      case _ => throw DeserializationException("Expected game status string")

  implicit val gameStateFormat: RootJsonFormat[GameState] = jsonFormat13(GameState.apply)
  
  case class KeyPressMessage(key: String)
  implicit val keyPressFormat: RootJsonFormat[KeyPressMessage] = jsonFormat1(KeyPressMessage.apply)
  
  case class GameConfigMessage(gridWidth: Int, gridHeight: Int, gameSpeed: Int)
  implicit val gameConfigFormat: RootJsonFormat[GameConfigMessage] = jsonFormat3(GameConfigMessage.apply)
  
  case class GameStateResponse(
    snake: List[Position],
    food: Option[Position], 
    score: Int,
    gameOver: Boolean,
    gameWon: Boolean,
    gridWidth: Int,
    gridHeight: Int,
    gameStarted: Boolean,
    connected: Boolean = true,
    stateNumber: Long
  )
  
  implicit val gameStateResponseFormat: RootJsonFormat[GameStateResponse] = jsonFormat10(GameStateResponse.apply)
