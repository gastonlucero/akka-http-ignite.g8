package ignite.utils

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigComponent {
  implicit val config: Config = ConfigFactory.load("application.conf")
}
