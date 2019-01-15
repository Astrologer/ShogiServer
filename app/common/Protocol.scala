package common

import akka.actor.ActorRef

object Protocol {
  case class ClientRequest(`type`: String, body: String)

  case class RegisterSocket(ref: ActorRef, gemeId: String)
  case class UnregisterSocket(ref: ActorRef)
  case class PlayerMove(ref: ActorRef, move: String)

  case class GameState(gameId: String, sfen: String, action: String)
}
