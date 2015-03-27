package com.sigfig.sync

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import spray.httpx.marshalling.BasicMarshallers._
import spray.routing._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.duration._
import scala.util.Random


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class SyncServiceActor(val syncManager: ActorRef, val userSyncRegion: ActorRef) extends Actor with ActorLogging with MyService {

    // the HttpService trait defines only one abstract member, which
    // connects the services environment to the enclosing actor or test
    def actorRefFactory = context

    // this actor only runs our route, but you could add
    // other things here, like request stream processing
    // or timeout handling
    def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService { this: SyncServiceActor =>

    import context.dispatcher
    import spray.json._
    import DefaultJsonProtocol._

    val syncManager: ActorRef
    val userSyncRegion: ActorRef
    implicit val timeout = Timeout(20.seconds)

    implicit def actorRefFactory: ActorContext

    implicit val statusFormat: RootJsonFormat[SyncStatus] = jsonFormat2(SyncStatus)

    val myRoute =
        path("sync") {
            parameter('num.as[Int]) { n: Int =>
                complete {

                    1 to n map { i =>
                        syncManager ! "Message"
                    }
                    "ok"
                }
            }
        } ~
        path("async") {
            complete {

                (syncManager ? "Message").mapTo[String]
            }

        } ~
        path("userSync") {
            complete {
                userSyncRegion ! Sync(Random.nextInt(100))
                "ok"
            }

        } ~
        path("syncStatus") {
            parameter('id.as[Int].?) { n =>
                complete {
                    (userSyncRegion ? GetSyncStatus(n.getOrElse(Random.nextInt(100)))).mapTo[SyncStatus]

                }
            }

        }
}
