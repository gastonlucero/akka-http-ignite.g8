package ignite.actor

import org.apache.ignite.cache.query.annotations.QuerySqlField

import scala.annotation.meta.field


sealed trait Iot

case class IotDevice(@(QuerySqlField@field)(index = true) name: String,
                     @(QuerySqlField@field)(index = true) gpio: String,
                     @(QuerySqlField@field) sensorType: String,
                     @(QuerySqlField@field) model: String) extends Iot

case class IotEvent(gpio: String,
                    data: String,
                    date:Long,
                    eventId: Int) extends Iot

case class PersistentEvent(iotEvent: IotEvent, metadata: Iot) extends Iot

object Messages {

  case object GetIotDevices

  case class GetIotDeviceByGpio(name: String)

  case class DeleteIotDevice(name: String)

  case class SaveIotDevice(iotDevice: IotDevice)

  case class GetIotDeviceBySql(model: String)

  case class GetEventsById(eventId: Int)

}
