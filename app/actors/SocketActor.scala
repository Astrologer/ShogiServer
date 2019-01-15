package actors

import akka.actor._
import common._
import common.Protocol._

object SocketActor {
  def props(sock: ActorRef, gate: ActorRef) = Props(new SocketActor(sock, gate))
}

class SocketActor(sock: ActorRef, gate: ActorRef) extends Actor {
  def receive = {
    case ClientRequest(tpe, body) => tpe match {
      case "subs" => gate ! RegisterSocket(sock, body)
      case "move" => gate ! PlayerMove(sock, body)
    }

    case _ => println("wrong message type")
  }
}
