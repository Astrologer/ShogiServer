package actors

import scala.collection.mutable.Map

import akka.actor._
import javax.inject.{Inject, Singleton}
import redis.clients.jedis.JedisPool

import shogi.Protocol._
import shogi.BoardState
import shogi.Json
import shogi.StorageClient

case class Player(id: String, gameId: String, isBlack: Boolean)

object GateActor {
  def props = Props[GateActor]
}

@Singleton
class GateActor @Inject()(store: StorageClient) extends Actor {
  val refs: Map[ActorRef, Player] = Map()
  val subs: Map[String, Set[ActorRef]] = Map()

  def receive = {
    case RegisterSocket(ref, gameId, isBlack) => register(ref, gameId, isBlack)
    case UnregisterSocket(ref) => unregister(ref)
    case PlayerMove(ref, move) => publish(ref, move)
    case msg @ StateMessage(gameId, sfet, action) => notifyAll(gameId, msg)

    case m @ _ => println(s"Gate: wrong message type $m")
  }

  def register(ref: ActorRef, gameId: String, isBlack: Boolean) {
    var id = s"${ref.hashCode}:${System.currentTimeMillis}"
    unregister(ref)

    // TODO check that game exists
    if (!store.setPlayer(gameId, id, isBlack))
      throw new Exception("Player already exists")

    refs(ref) = Player(id, gameId, isBlack)
    subs(gameId) = subs.getOrElse(gameId, Set()) + ref

    val sfen = store.getState(gameId)
    ref ! StateMessage(gameId, sfen, "").toRaw
  }

  def unregister(ref: ActorRef) {
    refs.remove(ref).foreach { player =>
        if (store.isPlayer(player.gameId, player.id, player.isBlack))
          store.removePlayer(player.gameId, player.isBlack)

        val listeners = subs(player.gameId).filter(_ != ref)
        if (listeners.size > 0) subs(player.gameId) = listeners
        else subs.remove(player.gameId)
    }
  }

  def publish(ref: ActorRef, move: String) {
    val player = refs(ref)

    if (!store.isPlayer(player.gameId, player.id, player.isBlack)) {
      println("player isn't active")
      return
    }

    val sfen = store.getState(player.gameId)
    val state = BoardState.fromSFEN(sfen)

    if (state.isLegalMove(move) && player.isBlack == state.isBlackMove) {
      val sfen = state.toSFEN(move)
      store.pushMove(player.gameId, move)
      store.setState(player.gameId, sfen)
      store.publish(Json.dumps(StateMessage(refs(ref).gameId, sfen, move).toRaw))
    } else println("illegal move")
  }

  def notifyAll(gameId: String, msg: StateMessage) {
    subs
      .getOrElse(gameId, Seq())
      .foreach(sock => sock ! StateMessage(msg.gameId, msg.sfen, msg.action).toRaw)
  }
}
