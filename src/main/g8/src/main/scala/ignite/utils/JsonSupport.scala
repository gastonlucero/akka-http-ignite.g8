package ignite.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ignite.actor.{IotDevice, IotEvent}
import spray.json.DefaultJsonProtocol

/**
  * This trait contains the parser for entities returned on  HttpRoutes
  * Uses internally spray-json
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val iotDeviceFormat = jsonFormat4(IotDevice)
  implicit val iotEventFormat = jsonFormat4(IotEvent)
}
