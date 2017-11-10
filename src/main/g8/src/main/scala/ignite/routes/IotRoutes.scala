package ignite.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{parameters, post, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import ignite.actor.Messages._
import ignite.actor.{IotDevice, IotEvent}
import ignite.utils.JsonSupport

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Akka Http endpoints
  */
trait IotRoutes extends JsonSupport {

  implicit def system: ActorSystem

  def iotActor: ActorRef

  // Required by the `ask` (?) method
  implicit lazy val timeoutRequest = Timeout(10 second)

  lazy val iotRoutes: Route =
    pathPrefix("devices") {
      path("all") {
        get {
          // GET -> /devices/all
          val futureResult: Future[List[IotDevice]] = (iotActor ? GetIotDevices)
            .mapTo[List[IotDevice]]
          complete(StatusCodes.OK, futureResult)
        }
      } ~
        pathEnd {
          // GET -> /devices?gpio=[gpio]
          (get & parameters('gpio.as[String])) { gpio =>
            val optionalDevice: Future[Option[IotDevice]] = (iotActor ? GetIotDeviceByGpio(gpio)).mapTo[Option[IotDevice]]
            rejectEmptyResponse {
              complete(optionalDevice)
            }
          }
        } ~
        // POST -> /devices
        post {
          entity(as[IotDevice]) {
            device => {
              iotActor ! SaveIotDevice(device)
              complete(201, "IotDevice created!!!") //201 = StatusCodes.Created
            }
          }
        }
    } ~
      // DELETE -> /device/[gpio]
      path("device" / Segment) {
        gpio => {
          delete {
            val deleted: Future[Boolean] = (iotActor ? DeleteIotDevice(gpio)).mapTo[Boolean]
            onSuccess(deleted) {
              result => complete(200, HttpEntity(s"IotDevice [$gpio] was deleted = [$result]"))
            }

          }
        }
      } ~
      // GET -> /sensorSql/[sensorType]
      path("sensorSql" / Segment) { (sensorType) =>
        get {
          val deviceBySql: Future[List[IotDevice]] = (iotActor ? GetIotDeviceBySql(sensorType)).mapTo[List[IotDevice]]
          onSuccess(deviceBySql) {
            resultSql =>
              complete(200, resultSql)
          }
        }
      } ~
      // POST -> /event
      path("event") {
        post {
          entity(as[IotEvent]) {
            event => {
              iotActor ! event
              complete(200, "ACK Ok")
            }
          }
        }
      }

}
