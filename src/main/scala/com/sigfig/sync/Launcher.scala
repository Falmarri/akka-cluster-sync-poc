package com.sigfig.sync

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import com.sigfig.sync.manager.BrokerageManager
import akka.routing.FromConfig
import akka.contrib.pattern.ClusterSingletonManager
import com.sigfig.sync.manager.SyncManager
import akka.contrib.pattern.ClusterSharding
import akka.actor.ActorRef
import com.sigfig.sync.actors.UserSyncActor
import akka.actor.PoisonPill
import akka.contrib.pattern.ClusterSingletonProxy
import com.sigfig.sync.actors.SyncWorker
import akka.actor.ActorPath
import akka.persistence.journal.leveldb.SharedLeveldbStore
import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import scala.util.Random
import scala.concurrent.duration._

object Launcher extends App {

    // we need an ActorSystem to host our application in

    def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
        // Start the shared journal one one node (don't crash this SPOF)
        // This will not be needed with a distributed journal
        if (startStore)
            system.actorOf(Props[SharedLeveldbStore], "store")
        // register the shared journal
        import system.dispatcher
        implicit val timeout = Timeout(60.seconds)
        val f = (system.actorSelection(path) ? Identify(None))
        f.onSuccess {
            case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
            case _ =>
                system.log.error("Shared journal not started at {}", path)
                system.terminate()
        }
        f.onFailure {
            case _ =>
                system.log.error("Lookup of shared journal at {} timed out", path)
                system.terminate()
        }
    }


    implicit val system = ActorSystem.create("SyncServer", ConfigFactory.
        parseString(s"akka.remote.netty.tcp.port=${args(0) }").
        withFallback(ConfigFactory.parseString("akka.cluster.roles = [sync]")).
        withFallback(ConfigFactory.load()))


    val syncWorkers = system.actorOf(FromConfig.props(Props[SyncWorker]), name = "syncWorker")

    val syncManager = system.actorOf(ClusterSingletonManager.props(
        singletonProps = Props(classOf[SyncManager], syncWorkers),
        singletonName = "syncManagerActor",
        terminationMessage = PoisonPill,
        role = None),
        name = "singleton"
    )


    startupSharedJournal(system, startStore = ("2551".equals(args(0))), path =
        ActorPath.fromString("akka.tcp://SyncServer@127.0.0.1:2551/user/store"))

    val syncManagerProxy = system.actorOf(ClusterSingletonProxy.props(
        role = None,
        singletonPath = "/user/singleton/syncManagerActor"))

    val userSyncRegion: ActorRef = ClusterSharding(system).start(
        typeName = "UserSyncActor",
        entryProps = if ("2551".equals(args(0))) None else Some(Props(classOf[UserSyncActor], syncManagerProxy)),
        roleOverride = None,
        rememberEntries = false,
        idExtractor = UserSyncActor.idExtractor,
        shardResolver = UserSyncActor.shardResolver)

    if ("2551".equals(args(0))) {
        import system.dispatcher
        system.scheduler.schedule(30.seconds, 1.seconds) {

            userSyncRegion ! Sync(Random.nextInt(10))
        }
    }
    val service = system.actorOf(Props(classOf[SyncServiceActor], syncManagerProxy, userSyncRegion), "http")


    implicit val timeout = Timeout(20.seconds)
    // start a new HTTP server on port 8080 with our service actor as the handler
    IO(Http) ? Http.Bind(service, interface = "127.0.0.1", port = args(1).toInt)

}