package reactivehub.akka.stream.apns.pusher

import java.nio.charset.StandardCharsets.UTF_8
import java.util.{Map => JMap}
import org.apache.kafka.common.serialization.Serializer
import reactivehub.akka.stream.apns.pusher.PushDataJsonFormats._

object PushDataSerializer extends Serializer[PushData] {
  override def configure(configs: JMap[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()

  override def serialize(topic: String, data: PushData): Array[Byte] =
    pushDataJsonFormat.write(data).compactPrint.getBytes(UTF_8)
}
