package actors

import scala.collection.mutable.Map

import akka.actor._
import javax.inject.{Inject, Singleton}
import redis.clients.jedis.JedisPool

import common.Protocol._

object StorageActor {
  def props = Props[StorageActor]
}

@Singleton
class StorageActor @Inject()(jedisPool: JedisPool) extends Actor {
  val refs: Map[ActorRef, String] = Map()
  val subs: Map[String, Set[ActorRef]] = Map()

  def receive = {
    case RegisterSocket(ref, gameId) =>
      if (refs.contains(ref)) {
        val id = refs(ref)
        subs(id) = subs(id).filter(_ != ref)
      }
      refs(ref) = gameId
      subs(gameId) = subs.getOrElse(gameId, Set()) + ref

    case PlayerMove(ref, move) =>
      publish(refs(ref), move)

    case msg @ GameState(gameId, sfet, action) =>
      subs
        .getOrElse(gameId, Seq())
        .foreach(sock => sock ! msg)

    case _ => println("st wrong message type")
  }

  def publish(gameId: String, move: String) {
    val jedis = jedisPool.getResource()
    jedis.publish("moves", s"${gameId}:${move}")
    jedis.close()
  }
}
