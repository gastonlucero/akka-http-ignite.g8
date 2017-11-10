package ignite

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ignite.actor.{IotActor, IotDevice, IotEvent}
import ignite.routes.IotRoutes
import ignite.utils.IgniteHelper._
import ignite.utils.JsonSupport
import org.apache.ignite.Ignition
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

/**
  * *
  * Basic structure of tests:
  * REQUEST ~> ROUTE ~> check {
  * ASSERTIONS
  * }
  */
class IotRoutesSpec extends WordSpec with Matchers with BeforeAndAfterAll with ScalaFutures with ScalatestRouteTest with JsonSupport with IotRoutes {

  override def afterAll() = {
    system.terminate()
  }

  override val iotActor: ActorRef = system.actorOf(IotActor.props(igniteDataGrid(name = "test", metrics = false)))

  lazy val routes = iotRoutes

  "IotRoutes" should {
    val iotTest = IotDevice(name = "deviceTest", gpio = "gpioTest", sensorType = "typeTest", model = "modelTest")
    val iotEvent = IotEvent(gpio = "gpioTest", data = "{\"lon\": 40.418102, \"lat\": -3.714441, \"value\": 15}", date = System.currentTimeMillis(), eventId = 2)

    "Return no devices is cache is empty (GET /devices/all)" in {
      Get("/devices/all") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        responseAs[String] shouldEqual "[]"
      }
    }

    "Return created code  (POST /devices)" in {
      Post("/devices", iotTest) ~> routes ~> check {
        status should ===(StatusCodes.Created)
        responseAs[String] shouldEqual "IotDevice created!!!"
      }
    }

    "Find by name (GET /devices?gpio=[gpio])" in {
      Get("/devices?gpio=gpioTest") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[IotDevice] shouldEqual iotTest
      }
    }

    "Find by model with sql (GET /sensorSql/[sensorType])" in {
      Get("/sensorSql/typeTest") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        entityAs[List[IotDevice]].size shouldBe 1
      }
    }

    "Post Event (POST /event)" in {
      Post("/event", iotEvent) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        responseAs[String] shouldEqual "ACK Ok"
      }
    }

    "Delete cache value  by name (DELETE /device/[gpio])" in {
      Delete("/device/gpioTest") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        responseAs[String] shouldEqual s"IotDevice [gpioTest] was deleted = [true]"
      }
    }
  }
}
