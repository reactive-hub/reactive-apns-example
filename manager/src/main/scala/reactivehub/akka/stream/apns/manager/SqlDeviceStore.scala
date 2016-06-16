package reactivehub.akka.stream.apns.manager

import akka.NotUsed
import akka.stream.scaladsl.Source
import scala.concurrent.{ExecutionContext, Future}
import slick.driver.H2Driver.api._

class SqlDeviceStore(db: Database)(implicit ec: ExecutionContext)
  extends DeviceStore {

  override def createDevices(newDevices: List[NewDevice]): Future[List[Long]] =
    db.run(DBIO.sequence(newDevices.map(insertNew))).map(_.toList)

  private def insertNew(newDevice: NewDevice): DBIO[Long] =
    devices.filter(_.token === newDevice.token).map(_.id).take(1).result
      .map(_.headOption)
      .flatMap {
        case Some(id) => DBIO.successful(id)
        case None => devices returning devices.map(_.id) += ((0, newDevice.token))
      }

  override def allDevices(): Source[Device, NotUsed] =
    Source.fromPublisher(db.stream(devices.result)).map {
      case (id, token) => Device(id, token)
    }
}
