package shogi

import akka.actor.ActorRef

object Protocol {
  case class RawMessage(`type`: String, arg1: Option[String] = None, arg2: Option[String] = None, arg3: Option[String] = None) {
    def toMessage: Option[Message] = {
      val msg = `type` match {
        case "state" => for (gameId <- arg1; sfen <- arg2; action <- arg3) yield StateMessage(gameId, sfen, action)
        case "subs" => for (gameId <- arg1; isBlack <- arg2) yield SubscribeMessage(gameId, isBlack)
        case "move" => for (gameId <- arg1; move <- arg2) yield MoveMessage(gameId, move)
        case "ping" => for (id <- arg1) yield PingMessage(id)

        case _ => None
      }
      if (msg.isEmpty)
        println(s"Mainformed message $this")

      msg
    }
  }

  sealed abstract class Message(val `type`: String, val arg1: Option[String] = None, val arg2: Option[String] = None, val arg3: Option[String] = None) {
    def toRaw = {
      println(s"message -> $this")
      RawMessage(`type`, arg1, arg2, arg3)
    }
  }

  case class PingMessage(id: String) extends Message("ping", Some(id))
  case class PongMessage(id: String) extends Message("pong", Some(id))
  case class StateMessage(gameId: String, sfen: String, action: String) extends Message("state", Some(gameId), Some(sfen), Some(action))
  case class SubscribeMessage(gameId: String, isBlack: String) extends Message("state", Some(gameId), Some(isBlack))
  case class MoveMessage(gameId: String, move: String) extends Message("move", Some(gameId), Some(move))

  case class RegisterSocket(ref: ActorRef, gemeId: String, isBlack: Boolean)
  case class UnregisterSocket(ref: ActorRef)
  case class PlayerMove(ref: ActorRef, move: String)
}
