## Akka-Http template with Apache Ignite

Simple project that uses akka-http as entry point, interacts with akka-actor to handle messages, and finally uses apache-ignite distributed caches to store data.

##### Project Overview

This example emulates an Iot endpoint, contains functionality for adding a device, retrieving devices (using ignite sql features), or deleting a device,and also for simulates receiving events. 
Akka Http endpoints are defined with a domain-specific language (DSL) to simplify the definition.In the example IotRoutes class is the endpoint
The main class is IgniteAkkaMain,and when starts up, create IotActor, which is the handler of the messages that arrives from the different
endpoints,and contains the business logic. This actor maintains a reference to igniteCache, that is used in all messages for put, get , query or delete entries. 

##### Configuration
Resources folder contains <u>application.conf</u> file, this file is where we put the configuration properties, for example for akka: 
   
    akka.port = http port where the server is listening request
   
Related to Ignite, there is a particular configuration property:

    ignite.dataGrid.type = simple | cluster | multicast   

#### Akka Http - Akka Actor
Iot Routes trait define the endpoints, using DSL to write routes. The bind is doing when http server is created

   
    lazy val routes: Route = iotRoutes // This attributes belong to IotRoutes trait
    
    val port = config.getInt("akka.port")
    val serverBindingFuture: Future[ServerBinding] = Http().bindAndHandle(routes, "localhost", port) 


In essence routes are defined with directives:

    This route is a GET request to http://localhost:8080/devices/all :   
    pathPrefix("devices") { //This is a directive
          path("all") { //This is another directive
            get { //This is another directive
              ...
              complete(...) 
            }
           }
            ...
            
For complete the request, a message is send to iotActor, that contains the business logic,it make uses of ignite to put , retrieve and delete values, and also uses ignite sql, for more complex queries.


        case GetIotDeviceBySql(sensorType) => {
              val sqlText = s"sensorType = $sensorType"
              println(s"Get values using Ignite Sql $sqlText")
              val sql = new SqlQuery[String, IotDevice](classOf[IotDevice], sqlText)
              sender() ! igniteCache.query(sql).getAll.asScala.map(_.getValue)
         }
                         
#### Ignite
###### Mode 'simple'    

Ignite cache by default starts an instance in the local machine where the app is running,start the app and console prints this message
 
    Topology snapshot [ver=1, servers=1, clients=0, CPUs=4, heap=1.8GB]
    Hello I am the IotActor[Actor[akka://IgniteActorSystem/user/iotActor#1059495084]] with Ignite [iotDeviceDataGrid]
    Server online at http://0.0.0.0:8080
    
 but ignite node can automatically discover each other, so, start another instance of the app, changing akka.port property first, for example to port 8090. you will see in this console something like this message, that indicates the cluster size is 2: 

    Topology snapshot [ver=2, servers=2, clients=0, CPUs=4, heap=3.6GB]
    Hello I am the IotActor[Actor[akka://IgniteActorSystem/user/iotActor#-1244314546]] with Ignite [iotDeviceDataGrid]
    Server online at http://0.0.0.0:8090
    
And in node1 console:

    ...
    Topology snapshot [ver=2, servers=2, clients=0, CPUs=4, heap=3.6GB]
       
 Now we have an Ignite cluster!,what this means? simple, if we do cache.put("key","value") in node1, and after that a cache.get("key") in node2, we obtain "value".
 
###### Mode 'cluster'
With this mode, ignite is able to uses cluster groups, that represents a logical grouping of cluster nodes. In this case, when 'cluster' mode is enabled, the behavior is the same as prior mode, but with the ability to send messages to cluster group, in this case, only to remotes nodes.

Run again the app, with 'cluster' mode, first with akka.port=8080 and again with akka.port=8090, the messages are the same, but with a little difference in node1:

*Node1:    
       
       Topology snapshot [ver=1, servers=1, clients=0, CPUs=4, heap=1.8GB]
       
       Hello node local8080, this message had been send by igniteCompute broadcast
          
       Hello I am the IotActor[Actor[akka://IgniteActorSystem/user/iotActor#1522453866]] with Ignite [iotDeviceDataGrid]
       Server online at http://0.0.0.0:8080
       Topology snapshot [ver=2, servers=2, clients=0, CPUs=4, heap=3.6GB]
       Hello node local8080, this message had been send by igniteCompute broadcast  
       
*Node2:

       Topology snapshot [ver=2, servers=2, clients=0, CPUs=4, heap=3.6GB]
       Hello node local8090, this message had been send by igniteCompute broadcast
       Hello I am the IotActor[Actor[akka://IgniteActorSystem/user/iotActor#-187060348]] with Ignite [iotDeviceDataGrid]
       Server online at http://0.0.0.0:8090            
         
IgniteHelper.igniteCluster method, sends to remote nodes in the cluster(not the current node) this message when node starts:
 
     igniteCluster.forRemotes().ignite().compute().broadcast(new IgniteRunnable {
       override def run(): Unit = println(s"Hello node ${igniteCluster.localNode().consistentId()}, this message had been send by igniteCompute broadcast")
     })        

Ignite can distribute your computations across cluster nodes
 <a>https://apacheignite.readme.io/docs/compute-grid
 
 ###### Mode 'multicast'
 
 Nodes can discover each other by using DiscoverySpi. Ignite provides TcpDiscoverySpi as a default implementation of DiscoverySpi that uses TCP/IP for node discovery. Discovery SPI can be configured for Multicast and Static IP based node discovery.
 Multicast Based Discovery : TcpDiscoveryMulticastIpFinder uses Multicast to discover other nodes in the grid and is the default IP finder.

###### Sql Support

Ignite supports sql over values, the way an app can achieve this is indexing a type class
        
        cacheConfig.setIndexedTypes(Seq(classOf[String], classOf[IotDevice]): _*) 

IotDevice class needs some annotations to be available for queries

        case class IotDevice(@(QuerySqlField@field)(index = true) name: String,
                             @(QuerySqlField@field)(index = true) gpio: String,
                             @(QuerySqlField@field) sensorType: String,
                             @(QuerySqlField@field) model: String) extends Iot


#### ScalaTest

IotRoutesSpec class contains test for all endpoints.                                                          
         
#### Curl test

*Create an IotDevice and put in cache with gpio as key-entry

    curl -H "Content-type: application/json" -X POST -d '{"name": "fridge-sensor", "gpio":"gpio57","model":"raspberry 3.0","sensorType":"temperature"}' http://localhost:8080/devices
    curl -H "Content-type: application/json" -X POST -d '{"name": "gps-sensor", "gpio":"gv300A","model":"gv300","sensorType":"gps"}' http://localhost:8080/devices
    
*Search all iotDevice

    curl http://localhost:8080/devices/all
    
*Search by name

    curl http://localhost:8080/devices?name=fridge-sensor
    
*Search by model with ignite sql

    curl http://localhost:8080/sensorSql/gps
    
*Delete an entry from cache with gpio value

    curl -X DELETE http://localhost:8080/device/gpio57
    
*Insert an event

    curl -H "Content-type: application/json" -X POST -d '{"gpio":"gpio57","data" : "{lon: 40.418,lat: -3.714,value: 15}","date":1510267094632,"eventId":2}' http://localhost:8080/event
