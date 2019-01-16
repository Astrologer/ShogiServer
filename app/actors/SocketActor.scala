package actors

import akka.actor._
import shogi.Protocol._

object SocketActor {
  def props(sock: ActorRef, gate: ActorRef) = Props(new SocketActor(sock, gate))
}

class SocketActor(sock: ActorRef, gate: ActorRef) extends Actor {
  def receive = {
    case ClientRequest("subs", body, Some(isBlack)) => gate ! RegisterSocket(sock, body, isBlack)
    case ClientRequest("move", body, None) => gate ! PlayerMove(sock, body)
    case ClientRequest("ping", body, None) =>

    case _ => println("Sock: wrong message type")
  }

  override def postStop() {
    gate ! UnregisterSocket(sock)
  }
}
