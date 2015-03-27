package com.sigfig.sync.manager

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import akka.persistence.PersistentActor
import akka.contrib.pattern.DistributedPubSubMediator



class BrokerageManager(brokerageData: Any) extends Actor with ActorLogging {
    
    def receive = {
        case message => 
            log.info("Received Message {} in Actor {}. {}", message, self.path.name, brokerageData)
             sender ! "ok"
    }   
}


//class BrokerageStatistics extends PersistentActor with ActorLogging {
//    
//    import DistributedPubSubMediator.{ Subscribe, SubscribeAck }
//    
//    override def persistenceId: String = self.path.parent.parent.name + "-" + self.path.name
//    
//    
//    
//    
//    
//    
//}