package shogi

import javax.inject.{Singleton, Inject, Named}
import akka.actor.ActorRef

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import actors.GateActor
import shogi.Protocol._

class GameListener @Inject()(store: StorageClient, @Named("gate-actor") gate: ActorRef) {
  def handler(message: String) {
     println(s" # ${message}, ${this}")
     gate ! Json.loads[GameState](message)
  }

  val listener = Future { store.subscribe(handler) }
}
