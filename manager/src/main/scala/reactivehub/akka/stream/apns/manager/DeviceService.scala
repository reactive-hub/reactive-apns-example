package reactivehub.akka.stream.apns.manager

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import reactivehub.akka.stream.apns.manager.DeviceService._
import reactivehub.akka.stream.apns.pusher.PushData
import scala.concurrent.Future
import scala.util.{Failure, Success}

object DeviceService {
  case class CreateDevices(devices: List[NewDevice])

  sealed trait CreateDevicesResponse
  case class DevicesCreated(ids: List[Long]) extends CreateDevicesResponse

  case object PingDevices

  def props(store: DeviceStore, queue: PushQueue)(implicit m: ActorMaterializer): Props =
    Props(classOf[DeviceService], store, queue, m)
}

class DeviceService(store: DeviceStore, queue: PushQueue)(implicit m: ActorMaterializer)
  extends Actor with ActorLogging {

  import context.dispatcher

  override def receive: Receive = {
    case CreateDevices(devices) =>
      log.debug("Creating new devices for tokens {}", devices.map(_.token).mkString(", "))
      store.createDevices(devices).map(DevicesCreated).pipeTo(sender())

    case PingDevices =>
      log.debug("Pinging all devices")
      store.allDevices()
        .map(device => device.id -> PushData(device.token, Some("Hello"), Some(1)))
        .runWith(queue.pushDataSink)
        .onComplete {
          case Success(num) => log.debug("{} devices pinged", num)
          case Failure(t) => log.error(t, "Failed to ping devices")
        }
  }
}

trait DeviceStore {
  def createDevices(devices: List[NewDevice]): Future[List[Long]]
  def allDevices(): Source[Device, NotUsed]
}

trait PushQueue {
  def pushDataSink: Sink[(Long, PushData), Future[Long]]
}
