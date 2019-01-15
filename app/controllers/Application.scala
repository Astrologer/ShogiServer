package controllers

import javax.inject._
import play.api.mvc._
import play.api._
import play.api.libs.streams.ActorFlow
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.WebSocket.MessageFlowTransformer
import akka.actor.ActorRef

import actors.SocketActor
import common.Protocol._

class Application @Inject()(@Named("gate-actor") gate: ActorRef, cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {
  implicit val inEventFormat = Json.format[ClientRequest]
  implicit val outEventFormat = Json.format[GameState]
  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[ClientRequest, GameState]

  def index = Action { implicit request =>
    Ok("Your new Scala application is ready!")
  }

  def socket = WebSocket.accept[ClientRequest, GameState] { request =>
    ActorFlow.actorRef { sock =>
      SocketActor.props(sock, gate)
    }
  }
}
