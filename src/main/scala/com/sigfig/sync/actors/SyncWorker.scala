package com.sigfig.sync.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import com.sigfig.sync.{Sync, SyncResult}

class SyncWorker extends Actor with ActorLogging {
    
    def receive = {
        case s@Sync(portfolioUserId) => {
            log.info("Received message {} in Actor {}.", s, self.path.name)
            Thread sleep 5000
            log.info("Replying to {} in Actor {}.", sender.path, self.path.name)
            sender ! SyncResult(portfolioUserId, "ok")
            
        }
    }
}
