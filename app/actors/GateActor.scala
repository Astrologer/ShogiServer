package actors

import scala.collection.mutable.Map

import akka.actor._
import javax.inject.{Inject, Singleton}
import redis.clients.jedis.JedisPool

import shogi.Protocol._
import shogi.Json
import shogi.StorageClient

object GateActor {
  def props = Props[GateActor]
}

@Singleton
class GateActor @Inject()(store: StorageClient) extends Actor {
  val refs: Map[ActorRef, String] = Map()
  val subs: Map[String, Set[ActorRef]] = Map()

  def receive = {
    case RegisterSocket(ref, gameId, isBlack) => register(ref, gameId, isBlack)
    case UnregisterSocket(ref) => unregister(ref)
    case PlayerMove(ref, move) => publish(ref, move)
    case msg @ GameState(gameId, sfet, action) => notifyAll(gameId, msg)

    case _ => println("Gate: wrong message type")
  }

  def register(ref: ActorRef, gameId: String, isBlack: Boolean) {
    unregister(ref)
    refs(ref) = gameId
    subs(gameId) = subs.getOrElse(gameId, Set()) + ref
  }

  def unregister(ref: ActorRef) {
    println(s"unregistering ${ref}")
    refs.remove(ref).foreach { id =>
        val listeners = subs(id).filter(_ != ref)
        if (listeners.size > 0) subs(id) = listeners
        else subs.remove(id)
    }
  }

  def publish(ref: ActorRef, move: String) {
    store.publish(Json.dumps(GameState(refs(ref), "sfen", move)))
  }

  def notifyAll(gameId: String, msg: GameState) {
    subs
      .getOrElse(gameId, Seq())
      .foreach(sock => sock ! msg)
  }
}
