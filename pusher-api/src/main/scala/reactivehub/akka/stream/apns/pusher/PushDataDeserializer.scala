package reactivehub.akka.stream.apns.pusher

import java.nio.charset.StandardCharsets.UTF_8
import java.util.{Map => JMap}
import org.apache.kafka.common.serialization.Deserializer
import reactivehub.akka.stream.apns.pusher.PushDataJsonFormats._
import spray.json._

object PushDataDeserializer extends Deserializer[PushData] {
  override def configure(configs: JMap[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()

  override def deserialize(topic: String, data: Array[Byte]): PushData =
    pushDataJsonFormat.read(new String(data, UTF_8).parseJson)
}
