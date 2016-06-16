package reactivehub.akka.stream.apns.manager

import slick.driver.H2Driver.api._
import slick.lifted.ProvenShape

class DeviceTable(tag: Tag) extends Table[(Long, String)](tag, "DEVICES") {
  def id: Rep[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def token: Rep[String] = column[String]("TOKEN")

  def * : ProvenShape[(Long, String)] = (id, token)
  def idx = index("idx_token", token, unique = true)
}

object devices extends TableQuery(new DeviceTable(_))
