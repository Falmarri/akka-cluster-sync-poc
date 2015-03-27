package com.sigfig.sync

import akka.actor._
import akka.actor.SupervisorStrategy.Stop
import spray.http.StatusCodes._
import spray.routing.RequestContext
import akka.actor.OneForOneStrategy
import scala.concurrent.duration._
import com.sigfig.sync.PerRequest._
import spray.http.StatusCode


trait PerRequest extends Actor {

  import context._


  def r: RequestContext
  def target: ActorRef
  def message: Any

  setReceiveTimeout(2.seconds)
  target ! message

  def receive = {
    case ReceiveTimeout   => complete(GatewayTimeout, "Request timeout")
  }

  def complete[T <: AnyRef](status: StatusCode, obj: T) = {
    r.complete(status, obj.toString())
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        complete(InternalServerError, e.getMessage)
        Stop
      }
    }
}

object PerRequest {
  case class WithActorRef(r: RequestContext, target: ActorRef, message: Any) extends PerRequest

  case class WithProps(r: RequestContext, props: Props, message: Any) extends PerRequest {
    lazy val target = context.actorOf(props)
  }
}

trait PerRequestCreator {
  this: Actor =>

  def perRequest(r: RequestContext, target: ActorRef, message: Any) =
    context.actorOf(Props(new WithActorRef(r, target, message)))

  def perRequest(r: RequestContext, props: Props, message: Any) =
    context.actorOf(Props(new WithProps(r, props, message)))
}