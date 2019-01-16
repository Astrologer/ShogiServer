import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.Configuration
import javax.inject.{Singleton, Inject, Named}
import com.google.inject.name.Names
import com.google.inject.Provides
import java.net.URI
import akka.actor.ActorRef

import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPool

import actors.GateActor
import shogi.GameListener
import shogi.StorageClient

class Module extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[GateActor]("gate-actor")
    bind(classOf[GameListener]).asEagerSingleton();
    //bind(classOf[Client]).annotatedWith(Names.named("RedisClient")).in(classOf[Singleton])
  }

  @Inject
  @Provides
  @Singleton
  def getStorageClient(conf: Configuration): StorageClient = {
    val uri = URI.create(conf.get[String]("redis.url"))
    val poolConfig: JedisPoolConfig = new JedisPoolConfig()
    // TODO base pool configuration
    val pool = new JedisPool(poolConfig, uri)
    new StorageClient(pool, conf)
  }
}
