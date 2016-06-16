package reactivehub.akka.stream.apns.pusher

import spray.json.DefaultJsonProtocol

object PushDataJsonFormats
  extends DefaultJsonProtocol with PushDataJsonFormats

trait PushDataJsonFormats { this: DefaultJsonProtocol =>
  implicit val pushDataJsonFormat = jsonFormat3(PushData)
}
