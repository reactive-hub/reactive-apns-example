package reactivehub.akka.stream.apns.manager

case class Device(id: Long, token: String)

case class NewDevice(token: String)
