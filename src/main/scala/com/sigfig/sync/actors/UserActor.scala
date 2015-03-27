package com.sigfig.sync.actors

import akka.actor.{Stash, ActorLogging, Actor, ActorRef, Props}
import akka.contrib.pattern.ShardRegion
import akka.pattern.ask
import akka.persistence.PersistentActor
import com.sigfig.sync.{SyncStatus, SyncResult, GetSyncStatus, Sync, EntryEnvelope}


object UserSyncActor {

    val shardName: String = "Users"


    def props: Props = Props(classOf[UserSyncActor])


    val idExtractor: ShardRegion.IdExtractor = {
        case sync: Sync => (sync.portfolioUserId.toString, sync)
        case EntryEnvelope(id, payload) => (id.toString, payload)
    }

    val shardResolver: ShardRegion.ShardResolver = msg => msg match {
        case sync: Sync => (math.abs(sync.portfolioUserId.hashCode) % 10).toString
        case EntryEnvelope(id, payload) => (math.abs(id) % 10).toString
    }
}


class UserSyncActor(syncManager: ActorRef) extends Actor with Stash with ActorLogging {


    def syncing: Receive = {
        case m: Sync => stash

        case s@GetSyncStatus(id) =>
            log.info("Received message {} in Actor {}.", s, self.path.name)
            sender ! SyncStatus(id, "syncing")

        case r: SyncResult =>
            log.info("Received message {} in Actor {}.", r, self.path.name)
            unstashAll
            context.unbecome

    }

    def receive = {
        case s@Sync(portfolioUserId) =>
            log.info("Received message {} in Actor {}.", s, self.path.name)
            syncManager ! s
            context.become(syncing, discardOld = false)

        case s@GetSyncStatus(id) =>
            log.info("Received message {} in Actor {}.", s, self.path.name)
            sender ! SyncStatus(id, "OK")

        case m =>
            log.error("Received unrecognized message {}.", m)

    }


}