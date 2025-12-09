package snake.http

import snake.core.{GameState, Position, Direction, GameStatus, GameConfig}
import spray.json.*

import scala.collection.immutable.{List => ScalaList, Nil => ScalaNil}
import scala.{Option => ScalaOption, None => ScalaNone, Some => ScalaSome}

import stainless.lang._
import stainless.collection._
import stainless.annotation._

object JsonProtocol extends DefaultJsonProtocol:

  private def convertToScalaList[T](ssList: List[T]): ScalaList[T] =
    ssList match
      case Nil()            => ScalaList.empty[T]
      case Cons(head, tail) => head :: convertToScalaList(tail)

  private def convertToStainlessList[T](scList: ScalaList[T]): List[T] =
    if scList.isEmpty then Nil[T]()
    else Cons(scList.head, convertToStainlessList(scList.tail))

  implicit def stainlessListFormat[T: JsonFormat]: RootJsonFormat[List[T]] =
    new RootJsonFormat[List[T]] {
      private val scalaListFormat: RootJsonFormat[ScalaList[T]] =
        implicitly[RootJsonFormat[ScalaList[T]]]

      def write(ssList: List[T]): JsValue = {
        scalaListFormat.write(convertToScalaList(ssList))
      }

      def read(json: JsValue): List[T] = {
        convertToStainlessList(scalaListFormat.read(json))
      }
    }
  private def convertToScalaOption[T](ssOption: Option[T]): ScalaOption[T] =
    ssOption match {
      case None()      => ScalaNone
      case Some(value) => ScalaOption(value)
    }

  private def convertToStainlessOption[T](scOption: ScalaOption[T]): Option[T] =
    scOption match {
      case ScalaNone        => None()
      case ScalaSome(value) => Some(value)
    }

  implicit def stainlessOptionFormat[T: JsonFormat]: RootJsonFormat[Option[T]] =
    new RootJsonFormat[Option[T]] {
      private val scalaOptionFormat: JsonFormat[ScalaOption[T]] =
        implicitly[JsonFormat[ScalaOption[T]]]

      def write(ssOption: Option[T]): JsValue = {
        scalaOptionFormat.write(convertToScalaOption(ssOption))
      }

      def read(json: JsValue): stainless.lang.Option[T] = {
        convertToStainlessOption(scalaOptionFormat.read(json))
      }
    }

  implicit val positionFormat: RootJsonFormat[Position] = jsonFormat2(
    Position.apply
  )

  implicit val directionFormat: RootJsonFormat[Direction] =
    new RootJsonFormat[Direction]:
      def write(direction: Direction): JsValue = JsString(
        direction.toString.toLowerCase
      )
      def read(json: JsValue): Direction = json match
        case JsString("up")    => Direction.Up
        case JsString("down")  => Direction.Down
        case JsString("left")  => Direction.Left
        case JsString("right") => Direction.Right
        case _ => throw DeserializationException("Expected direction string")

  implicit val gameStatusFormat: RootJsonFormat[GameStatus] =
    new RootJsonFormat[GameStatus]:
      def write(status: GameStatus): JsValue = JsString(
        status.toString.toLowerCase
      )
      def read(json: JsValue): GameStatus = json match
        case JsString("waiting")  => GameStatus.Waiting
        case JsString("playing")  => GameStatus.Playing
        case JsString("gameover") => GameStatus.GameOver
        case JsString("gamewon")  => GameStatus.GameWon
        case _ => throw DeserializationException("Expected game status string")

  implicit val gameConfigFormat: RootJsonFormat[GameConfig] = jsonFormat3(
    GameConfig.apply
  )
  implicit val gameStateFormat: RootJsonFormat[GameState] = jsonFormat7(
    GameState.apply
  )

  case class KeyPressMessage(key: String)
  implicit val keyPressFormat: RootJsonFormat[KeyPressMessage] = jsonFormat1(
    KeyPressMessage.apply
  )

  case class GameConfigMessage(
      gridWidth: Int,
      gridHeight: Int,
      gameSpeed: Int
  )
  implicit val gameConfigMessageFormat: RootJsonFormat[GameConfigMessage] =
    jsonFormat3(GameConfigMessage.apply)

  case class GameStateResponse(
      snake: List[Position],
      food: Option[Position],
      gameOver: Boolean,
      gameWon: Boolean,
      gridWidth: Int,
      gridHeight: Int,
      gameStarted: Boolean,
      connected: Boolean = true,
      stateNumber: Long
  )
  implicit val gameStateResponseFormat: RootJsonFormat[GameStateResponse] =
    jsonFormat9(GameStateResponse.apply)
