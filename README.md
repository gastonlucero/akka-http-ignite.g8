A [Giter8][g8] template for Akka Http with Apache Ignite!

   sbt new gastonlucero/akka-http-ignite.g8

# Akka-Http template with Apache Ignite

<u>Curl for testing

<b>Create an IotDevice and put in cache with gpio as key-entry

    curl -H "Content-type: application/json" -X POST -d '{"name": "fridge-sensor", "gpio":"gpio57","model":"raspberry 3.0","sensorType":"temperature"}' http://localhost:8080/devices

    curl -H "Content-type: application/json" -X POST -d '{"name": "gps-sensor", "gpio":"gv300A","model":"gv300","sensorType":"gps"}' http://localhost:8080/devices


<b>Search all iotDevice

    curl http://localhost:8080/devices/all


<b>Search by name

    curl http://localhost:8080/devices?name=fridge-sensor

<b>Search by model with ignite sql

    curl http://localhost:8080/sensorSql/gps

<b>Delete an entry from cache with gpio value

    curl -X DELETE http://localhost:8080/device/gpio57

<b>Insert an event

    curl -H "Content-type: application/json" -X POST -d '{"gpio":"gpio57","data" : "{lon: 40.418,lat: -3.714,value: 15}","date":1510267094632,"eventId":2}' http://localhost:8080/event




Template license
----------------
Written in 2017 by gaston.lucerom@gmail.com


To the extent possible under law, the author(s) have dedicated all copyright and related
and neighboring rights to this template to the public domain worldwide.
This template is distributed without any warranty. See <http://creativecommons.org/publicdomain/zero/1.0/>.

[g8]: http://www.foundweekends.org/giter8/
