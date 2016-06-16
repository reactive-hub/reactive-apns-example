package reactivehub.akka.stream.apns.manager

import spray.json.DefaultJsonProtocol

trait DeviceJsonFormats { this: DefaultJsonProtocol =>
  implicit val deviceJsonFormat = jsonFormat2(Device)
  implicit val newDeviceJsonFormat = jsonFormat1(NewDevice)
}
