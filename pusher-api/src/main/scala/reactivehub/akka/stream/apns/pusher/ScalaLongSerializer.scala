package reactivehub.akka.stream.apns.pusher

import java.util.{Map => JMap}
import org.apache.kafka.common.serialization.{LongSerializer, Serializer}

object ScalaLongSerializer extends Serializer[Long] {
  private val s = new LongSerializer

  override def configure(configs: JMap[String, _], isKey: Boolean): Unit =
    s.configure(configs, isKey)

  override def serialize(topic: String, data: Long): Array[Byte] =
    s.serialize(topic, data)

  override def close(): Unit = s.close()
}
