package ignite.actor

import akka.actor.{Actor, ActorRef, Props}
import ignite.actor.Messages._
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.{ScanQuery, SqlQuery}

import scala.collection.JavaConverters.{asScalaIteratorConverter, iterableAsScalaIterableConverter}


/**
  * For actor creation see {https://doc.akka.io/docs/akka/current/scala/actors.html#recommended-practices}.
  */
object IotActor {
  def props(igniteCache: IgniteCache[String, Iot]): Props = Props(classOf[IotActor], igniteCache)
}

class IotActor(igniteCache: IgniteCache[String, Iot]) extends Actor {

  override def preStart(): Unit = {
    println(s"Hello I am the IotActor[$self] with Ignite [${igniteCache.getName}]")
  }

  //In this case creation without companion object
  val persistActor: ActorRef = context.actorOf(Props[PersistActor])

  override def receive: Receive = {
    case GetIotDevices => {
      println("Get all values from cache")
      sender() ! igniteCache.iterator().asScala.map(_.getValue).toList
    }
    case GetIotDeviceByGpio(gpio) => {
      println("Get values from cache or return None")
      sender() ! Option(igniteCache.get(gpio))
    }
    case SaveIotDevice(device) => {
      println(s"Put in cache the value $device")
      igniteCache.put(device.gpio, device)
    }
    case GetIotDeviceBySql(sensorType) => {
      val sqlText = s"sensorType = '$sensorType'"
      println(s"Get values using Ignite Sql $sqlText")
      val sql = new SqlQuery[String, IotDevice](classOf[IotDevice], sqlText)
      sender() ! igniteCache.query(sql).getAll.asScala.map(_.getValue)
    }
    case DeleteIotDevice(name) => {
      println("If exists, delete from cache")
      sender() ! igniteCache.remove(name)
    }
    case event: IotEvent => {
      println("Retrive gpio metadata from cache if exists, else ignore the event, using Sql ScanQuery")
      //ScanQuery evaluates a predicate, in this case find the entry with key = event.gpio
      val cursor = igniteCache.query(new ScanQuery[String, IotDevice]((key, entryValue) => entryValue.gpio == event.gpio))
      val allEvents = cursor.getAll
      if (!allEvents.isEmpty) {
        val iotDevice = allEvents.asScala.map(_.getValue).toList.head
        persistActor ! PersistentEvent(event, iotDevice)
      } else {
        println(s"The IotDevice with gpio [${event.gpio}] does not exists in cache")
      }
    }
    case _ => unhandled()
  }


}
