package shogi

import javax.inject.{Singleton, Inject, Named}
import akka.actor.ActorRef

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import actors.GateActor
import shogi.Protocol._

class GameListener @Inject()(store: StorageClient, @Named("gate-actor") gate: ActorRef) {
  def handler(message: String) {
    Json.loads[RawMessage](message).toMessage match {
      case Some(msg) => gate ! msg
      case None => println(s"Listner: wrong message $message")
    }
  }

  val listener = Future { store.subscribe(handler) }
}
