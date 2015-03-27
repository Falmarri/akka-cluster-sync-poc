package com.sigfig.sync.manager

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.sigfig.sync.{Sync, SyncResult, SyncStatus}

class SyncManager(syncWorkers: ActorRef) extends Actor with ActorLogging {

    val userSyncActor = context.actorSelection("/user/syncWorker")

    def receive = {
        case s@Sync(portfolioUserId) =>
            log.info("Received message {} in Actor {}.", s, self.path.name)
            syncWorkers forward s
            sender ! "ok"

//         case s@SyncResult(portfolioUserId, payload) =>
//             log.info("Received message {} in Actor {}.", s, self.path.name)
//             userSyncActor ! s
    }
    
    

}