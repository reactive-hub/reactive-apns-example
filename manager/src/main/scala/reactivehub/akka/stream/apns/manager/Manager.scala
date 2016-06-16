package reactivehub.akka.stream.apns.manager

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.kafka.ProducerSettings
import akka.stream.ActorMaterializer
import akka.util.Timeout
import reactivehub.akka.stream.apns.pusher._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.Success
import slick.driver.H2Driver.api._

object Manager extends RestApi {
  val dbConfig = "h2"
  val kafka = "192.168.99.100:9092"
  val topic = "notifications"
  val interface = "0.0.0.0"
  val port = 8080

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher
  implicit val timeout = Timeout(10.second)

  def main(args: Array[String]): Unit = {
    val binding = createService().flatMap(manager => bind(manager))
    binding.onComplete {
      case Success(b) =>
        println(s"Successfully bound to ${b.localAddress}, press enter to exit")
      case _ =>
        println("Failed to bootstrap Manager, press enter")
    }

    StdIn.readLine()
    binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }

  private def createService(): Future[ActorRef] = {
    val db = Database.forConfig(dbConfig)
    val store = new SqlDeviceStore(db)
    val queue = new KafkaPushQueue(topic, producerSettings)

    db.run(devices.schema.drop)
      .recover({ case _ => () })
      .flatMap(_ => db.run(devices.schema.create))
      .map(_ => system.actorOf(DeviceService.props(store, queue)))
  }

  private def bind(service: ActorRef): Future[ServerBinding] =
    Http(system).bindAndHandle(route(service), interface, port)

  private def producerSettings: ProducerSettings[Long, PushData] =
    ProducerSettings(system, ScalaLongSerializer, PushDataSerializer)
      .withBootstrapServers(kafka)
}
