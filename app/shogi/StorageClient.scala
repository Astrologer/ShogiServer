package shogi

import play.api.Configuration
import javax.inject.{Singleton, Inject, Named}
import scala.collection.JavaConverters._
import scala.util.Random

import redis.clients.jedis.JedisPubSub
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Jedis

/**
 * TODO data model
 * gameId - INCR on `gameIndex` during game creation
 *
 * Fields:
 *    + gameIndex - <index> // INCR
 *    <gameId>:blackPlayer - "<timestaml><actorId>" // SETNX, cleanup by owner on socket close
 *    <gameId>:whitePlayer - "<timestaml><actorId>" // SETNX, cleanup by owner on socket close
 *    <gameId>:move - "black" / "white" // updated only by player with the same color
 *    <gameId>:state - sfen // updated by active player
 *    <gameId>:history - List of moves // LPUSH
 *
 */
class StorageClient(pool: JedisPool, conf: Configuration) {
  val DEFAULT_SFEN = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b"
  val channel: String = conf.get[String]("redis.channel")
  val gameIndex: String = conf.get[String]("redis.gameIndex")

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

  def getStateKey(gameId: String): String = s"${gameId}:state"
  def getHistoryKey(gameId: String): String = s"${gameId}:history"

  def getPlayerKey(gameId: String, isBlack: Boolean): String = {
    val color = if (isBlack) "blackPlayer" else "whitePlayer"
    s"${gameId}:${color}"
  }

  def createGame(): String = {
    val gameId = f"${Random.alphanumeric.take(8).toSeq.mkString}${newGameId}%08d"
    setState(gameId, DEFAULT_SFEN)
    gameId
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

  def newGameId(): Long = {
    query[Long] { jedis =>
      jedis.incr(gameIndex)
    }
  }

  def setPlayer(gameId: String, playerId: String, isBlack: Boolean): Boolean = {
    query[Boolean] { jedis =>
      jedis.setnx(getPlayerKey(gameId, isBlack), playerId.toString) == 1
    }
  }

  def isPlayer(gameId: String, playerId: String, isBlack: Boolean): Boolean = {
    query[Boolean] { jedis =>
      jedis.get(getPlayerKey(gameId, isBlack)) == playerId.toString
    }
  }

  def removePlayer(gameId: String, isBlack: Boolean) {
    query[Unit] { jedis =>
      jedis.del(getPlayerKey(gameId, isBlack))
    }
  }

  def setState(gameId: String, sfen: String) {
    query[Unit] { jedis =>
      jedis.set(getStateKey(gameId), sfen)
    }
  }

  def getState(gameId: String): String = {
    query[String] { jedis =>
      jedis.get(getStateKey(gameId))
    }
  }

  def pushMove(gameId: String, move: String) {
    query[Unit] { jedis =>
      jedis.rpush(getHistoryKey(gameId), move)
    }
  }

  def getMoves(gameId: String): List[String] = {
    query[List[String]] { jedis =>
      jedis.lrange(getHistoryKey(gameId), 0, -1).asScala.toList
    }
  }
}
