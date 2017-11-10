package ignite

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import ignite.actor.IotActor
import ignite.routes.IotRoutes
import ignite.utils.ConfigComponent
import ignite.utils.IgniteHelper._

import scala.concurrent.Future

/**
  * Main class, wich one starts http server on the port defined in properties file.
  * Http routes are in {@see IotRoutes} trait, that internally use an akkaActor to perform actions. This actor uses
  * igniteCache as a  in-memory computing platform, that allow execute queries, among other things, like compute task
  */
object IgniteAkkaMain extends App with ConfigComponent with IotRoutes {

  implicit val system: ActorSystem = ActorSystem("IgniteActorSystem")

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /**
    * Needed for the Future and its methods flatMap/onComplete in the end
    * Why is good  se a custom dispatcher {https://doc.akka.io/docs/akka-http/10.0.10/scala/http/handling-blocking-operations-in-akka-http-rou .html#handling-blocking-operations-in-akka-http}
    */
  implicit val blockingDispatcher = system.dispatchers.lookup("akka.my-blocking-dispatcher")

  val iotActor = system.actorOf(IotActor.props(igniteDataGrid("iotDeviceDataGrid", false)), "iotActor")

  //Http routes
  lazy val routes: Route = iotRoutes

  val port = config.getInt("akka.port")
  val serverBindingFuture: Future[ServerBinding] = Http().bindAndHandle(routes, "localhost", port)

  println(s"Server online at http://0.0.0.0:$port")
}

