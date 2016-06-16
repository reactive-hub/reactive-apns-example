package reactivehub.akka.stream.apns.manager

import akka.kafka.ProducerMessage.Message
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import org.apache.kafka.clients.producer.ProducerRecord
import reactivehub.akka.stream.apns.pusher.PushData
import scala.concurrent.Future

class KafkaPushQueue(topic: String, settings: ProducerSettings[Long, PushData])
  extends PushQueue {

  override def pushDataSink: Sink[(Long, PushData), Future[Long]] =
    Flow[(Long, PushData)]
      .map {
        case (key, value) => Message(
          new ProducerRecord[Long, PushData]("notifications", key, value), key)
      }
      .via(Producer.flow(settings))
      .toMat(Sink.fold(0L)({ case (acc, _) => acc + 1}))(Keep.right)
}
