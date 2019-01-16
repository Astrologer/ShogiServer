package shogi

import akka.actor.ActorRef

object Protocol {
  case class ClientRequest(`type`: String, body: String, isBlack: Option[Boolean] = None)
  case class GameState(gameId: String, sfen: String, action: String)

  case class RegisterSocket(ref: ActorRef, gemeId: String, isBlack: Boolean)
  case class UnregisterSocket(ref: ActorRef)
  case class PlayerMove(ref: ActorRef, move: String)
}
