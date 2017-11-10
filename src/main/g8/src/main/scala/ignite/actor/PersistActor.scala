package ignite.actor

import akka.actor.Actor

/**
  * PersistActor
  */
class PersistActor extends Actor {

  override def receive = {
    case e: PersistentEvent => {
      println(s"Persist to db [$e] or you can use igniteStreamer and send values to Kafka for instance")
    }
  }

}
