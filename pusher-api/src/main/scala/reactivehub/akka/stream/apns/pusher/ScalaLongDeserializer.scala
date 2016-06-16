package reactivehub.akka.stream.apns.pusher

import java.util.{Map => JMap}
import org.apache.kafka.common.serialization.{Deserializer, LongDeserializer}

object ScalaLongDeserializer extends Deserializer[Long] {
  private val s = new LongDeserializer

  override def configure(configs: JMap[String, _], isKey: Boolean): Unit =
    s.configure(configs, isKey)

  override def deserialize(topic: String, data: Array[Byte]): Long =
    s.deserialize(topic, data)

  override def close(): Unit = s.close()
}
