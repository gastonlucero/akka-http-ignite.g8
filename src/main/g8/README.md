# Akka-Http template with Apache Ignite

<u>Curl for testing

<b>Create an IotDevice and put in cache with gpio as key-entry

    curl -H "Content-type: application/json" -X POST -d '{"name": "fridge-sensor", "gpio":"gpio57","model":"raspberry 3.0","sensorType":"temperature"}' http://localhost:8080/devices

    curl -H "Content-type: application/json" -X POST -d '{"name": "gps-sensor", "gpio":"gv300A","model":"gv300","sensorType":"gps"}' http://localhost:8080/devices


<b>Search all iotDevices
    
    curl http://localhost:8080/devices/all


<b>Search by name
     
    curl http://localhost:8080/devices?name=[name]

<b>Search by model with ignite sql 
     
    curl http://localhost:8080/sensorSql/[model]
  
<b>Delete an entry from cache with gpio value
     
    curl -X DELETE http://localhost:8080/device/[gpio]
    
<b>Insert an event 

    curl -H "Content-type: application/json" -X POST -d '{"gpio":"gpio57","data" : "{lon: 40.418,lat: -3.714,value: 15}","date":1510267094632,"eventId":2}' http://localhost:8080/event
    
    
