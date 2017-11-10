A [Giter8][g8] template for Akka Http with Apache Ignite in Scala

 [![Build Status](https://travis-ci.org/http4s/akka-http-ignite.g8.svg)](https://travis-ci.org/gastonlucero/akka-http-ignite.g8)

Prerequisites:
- Jdk 8
- [sbt 0.13.13 or higher](http://www.scala-sbt.org/documentation.html)

Open a console and run

    sbt new gastonlucero/akka-http-ignite.g8

Specifies the values you want or press Enter for default configuration:
- `name`: Name of the project (default 'akka-http-ignite').
- `scala_version`: Scala version for this project (default '2.12.3').
- `akka_http_version`: Akka HTTP version for this project (default '10.0.10').
- `ignite_version`: Apache Ignite version for this project (default '2.3.0').
- `organization`: Organization for this project.

This will create a directory with you new project ready to use.

The template contains sources for
- Create an in-memory distributed cache with Ignite
- Create rest-endpoints with akka-http Routes
- An internal akka-actor which handles all messages
- A simple test using akka-http-testkit and scalatest

Run the proyect

    sbt run

Or import as SBT project into your ide and run the main class

    IgniteAkkaMain

Related links

[Akka Http](https://doc.akka.io/docs/akka-http/10.0.10/scala/http/index.html)

[Apache Ignite](http://apacheignite.readme.io/docs)

Template license
----------------
Written in 2017 by gaston.lucerom@gmail.com

To the extent possible under law, the author(s) have dedicated all copyright and related
and neighboring rights to this template to the public domain worldwide.
This template is distributed without any warranty. See <http://creativecommons.org/publicdomain/zero/1.0/>.

[g8]: http://www.foundweekends.org/giter8/
