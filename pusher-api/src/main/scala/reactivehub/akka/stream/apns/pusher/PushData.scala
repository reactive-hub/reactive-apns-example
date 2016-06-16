package reactivehub.akka.stream.apns.pusher

case class PushData(token: String, alert: Option[String], badge: Option[Int])
