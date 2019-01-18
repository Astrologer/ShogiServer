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

  println("--------------------------------------------------------------------------------")
  //println(store.newGameId())
  //println(store.setPlayer("game111", "b", false))
  //println(store.isActivePlayer("game111", "b", false))
  //println(store.isBlackMove("game111"))
  //println(store.flipMove("game111", false))
  //println(store.isBlackMove("game111"))
  //println(store.setState("game111", "9/9/9/9/9/9/9/9/9 b p"))
  //println(store.getState("game111"))

  //println(store.pushMove("game111", "k5c5b"))
  //println(store.getMoves("game111"))

  val listener = Future { store.subscribe(handler) }
}
