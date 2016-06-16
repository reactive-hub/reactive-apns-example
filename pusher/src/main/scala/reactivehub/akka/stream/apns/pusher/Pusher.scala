package reactivehub.akka.stream.apns.pusher

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.ssl.SslContext
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import reactivehub.akka.stream.apns.Environment._
import reactivehub.akka.stream.apns.TlsUtil._
import reactivehub.akka.stream.apns._
import reactivehub.akka.stream.apns.marshallers.SprayJsonSupport

object Pusher extends SprayJsonSupport {
  val kafka = "192.168.99.100:9092"
  val clientId = "pusher1"
  val consumerGroup = "pusher"
  val topics = Set("notifications")

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  def main(args: Array[String]): Unit = {
    val group = new NioEventLoopGroup()
    val apns = ApnsExt(system).connection[Long](Development, sslContext, group)

    Consumer.atMostOnceSource(consumerSettings)
      .map(msg => msg.key -> toNotification(msg.value))
      .filter(_._2.deviceToken.bytes.length < 100)
      .viaMat(apns)(Keep.right)
      .log("pusher", _.toString())
      .to(Sink.ignore).run()
      .onComplete { _ =>
        group.shutdownGracefully()
        system.terminate()
      }
  }

  private def sslContext: SslContext =
    loadPkcs12FromResource("/cert.p12", "password")

  private def consumerSettings: ConsumerSettings[Long, PushData] =
    ConsumerSettings(system, ScalaLongDeserializer, PushDataDeserializer, topics)
      .withBootstrapServers(kafka)
      .withClientId(clientId)
      .withGroupId(consumerGroup)
      .withProperty(AUTO_OFFSET_RESET_CONFIG, "earliest")

  private def toNotification(pushData: PushData): Notification = {
    var builder = Payload.Builder()
    pushData.alert.foreach(alert => builder = builder.withAlert(alert))
    pushData.badge.foreach(badge => builder = builder.withBadge(badge))
    Notification(DeviceToken(pushData.token), builder.result)
  }
}
