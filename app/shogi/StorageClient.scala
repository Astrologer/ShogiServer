package shogi

import play.api.Configuration
import javax.inject.{Singleton, Inject, Named}

import redis.clients.jedis.JedisPubSub
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Jedis

/**
 * TODO data model
 * gameId - INCR on `gameIndex` during game creation
 *
 * Fields:
 *    gameIndex - <index> // INCR
 *    <gameId>:blackPlayer - "<timestaml><actorId>" // SETNX, cleanup by owner on socket close
 *    <gameId>:whitePlayer - "<timestaml><actorId>" // SETNX, cleanup by owner on socket close
 *    <gameId>:move - "black" / "white" // updated only by player with the same color
 *    <gameId>:state - sfen // updated by active player
 *    <gameId>:history - List of moves // LPUSH
 *
 */
class StorageClient(pool: JedisPool, conf: Configuration) {
  val channel: String = conf.get[String]("redis.channel")

  class MessageHandler(handler: String => Unit) extends JedisPubSub {
    override def onMessage(channel: String, message: String) {
      handler(message)
    }
  }

  protected def query[T](f: Jedis => T): T = {
    val jedis = pool.getResource()
    try {
      return f(jedis)
    } finally {
      jedis.close()
    }
  }

  def publish(json: String) {
    query[Unit] { jedis =>
      jedis.publish(channel, json)
    }
  }

  def subscribe(handler: String => Unit) {
    query[Unit] { jedis =>
      jedis.subscribe(new MessageHandler(handler), channel)
    }
  }
}
