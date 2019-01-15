package tasks

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.Configuration
import javax.inject.{Singleton, Inject, Named}
import com.google.inject.name.Names
import com.google.inject.Provides
import java.net.URI
import akka.actor.ActorRef

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPubSub
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Jedis

import actors.StorageActor
import common.Protocol._

class Client @Inject()(jedisPool: JedisPool, @Named("gate-actor") gate: ActorRef) {
  class Handler extends JedisPubSub {
    override def onMessage(channel: String, message: String) {
      println(s" #${channel}: ${message}, ${this}")
      gate ! GameState(message.split(":").head, "--", message.split(":").tail.head)
    }
  }

  val listner = run()

  def run(): Future[Unit] = {
    val jedis = jedisPool.getResource()
    Future { jedis.subscribe(new Handler, "moves") }
  }
}

class ActorsModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[StorageActor]("gate-actor")
    bind(classOf[Client]).asEagerSingleton();
    //bind(classOf[Client]).annotatedWith(Names.named("RedisClient")).in(classOf[Singleton])
  }

  @Provides
  @Singleton
  @Inject
  def getJedisPool(conf: Configuration): JedisPool = {
    val poolConfig: JedisPoolConfig = new JedisPoolConfig()
    new JedisPool(poolConfig, URI.create(conf.get[String]("redis.url")))
  }
}
