package models

import java.util.UUID

import scala.concurrent.Future

case class Tenant(id: String, name: String, description: String, root: String)

object Tenant {

  val tenants = Seq(
    Tenant(
      UUID.randomUUID().toString,
      "Default Tenant",
      "The default tenant of API Mngr",
      "default"
    )
  )

  def findByRoot(root: String): Future[Option[Tenant]] = Future.successful(tenants.find(_.root == root))
  def findById(id: String): Future[Option[Tenant]] = Future.successful(tenants.find(_.id == id))
  def all(): Future[Seq[Tenant]] = Future.successful(tenants)
}
