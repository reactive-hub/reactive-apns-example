package reactivehub.akka.stream.apns.manager

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ContentTypes.`application/octet-stream`
import akka.http.scaladsl.model.Multipart
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Framing, Sink}
import akka.util.{ByteString, Timeout}
import reactivehub.akka.stream.apns.manager.DeviceService._
import scala.concurrent.ExecutionContext
import spray.json.DefaultJsonProtocol

trait RestApi extends SprayJsonSupport
  with DefaultJsonProtocol
  with DeviceJsonFormats {

  implicit def materializer: ActorMaterializer
  implicit def dispatcher: ExecutionContext
  implicit def timeout: Timeout

  def route(service: ActorRef): Route =
    path("jobs") {
      post {
        service ! PingDevices
        complete(Accepted)
      }
    } ~ path("devices") {
      post {
        entity(as[NewDevice]) { device =>
          val result = service.ask(CreateDevices(List(device)))
          complete(result.map(_ => Created))
        } ~ entity(as[Multipart.FormData]) { formData =>
          val result = formData.parts
            .filter(_.entity.contentType == `application/octet-stream`)
            .mapAsync(1) { bodyPart =>
              bodyPart.entity.dataBytes
                .via(Framing.delimiter(ByteString('\n'), 200))
                .map(bs => NewDevice(bs.utf8String))
                .grouped(10)
                .mapAsync(1)(devices => service.ask(CreateDevices(devices.toList)))
                .runWith(Sink.ignore)
            }.runWith(Sink.ignore)
          complete(result.map(_ => Created))
        }
      }
    }
}
