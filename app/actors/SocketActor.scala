package actors

import akka.actor._
import shogi.Protocol._

object SocketActor {
  def props(sock: ActorRef, gate: ActorRef) = Props(new SocketActor(sock, gate))
}

class SocketActor(sock: ActorRef, gate: ActorRef) extends Actor {
  def receive = {
    case m: RawMessage => {
      println(s"message <- $m")
      m.toMessage.map(_ match {
        case SubscribeMessage(gameId, isBlack) => gate ! RegisterSocket(sock, gameId, isBlack.toBoolean)
        case MoveMessage(gameId, move) => gate ! PlayerMove(sock, move)
        case PingMessage(id) => sock ! PongMessage(id).toRaw

        case StateMessage(gameId, sfen, action) =>
        case PongMessage(id) =>
      })
    }
    case m @ _ => println(s"Sock: wrong message type ${m}")
  }

  override def postStop() {
    gate ! UnregisterSocket(sock)
  }
}
