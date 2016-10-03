package models

import java.util.UUID

import play.api.libs.json.{JsArray, JsObject, JsValue}

import scala.concurrent.Future

case class Api(
  id: String,
  tenantId: String,
  name: String,
  description: String,
  version: String,
  prodHosts: Seq[String],
  sandboxHosts: Seq[String],
  port: Int,
  group: String,
  root: String,
  schemes: Seq[String],
  swagger: JsValue) {

  // println(this.tenantId, this.group, this.root.toLowerCase)

  def save(): Future[Unit] = {
    Api.apis = Api.apis :+ this
    Future.successful(())
  }
}

object Api {

  private var apis = Seq.empty[Api]

  def findByTenantAndGroupAndPath(tenantId: String, group: String, path: String): Future[Option[Api]] = {

    Future.successful(apis.find(api => {
      println(s"${api.tenantId} == ${tenantId}, ${api.group} == $group, ${path.toLowerCase()}.startsWith(${api.root.toLowerCase})")
      api.tenantId == tenantId && api.group == group && path.toLowerCase().startsWith(api.root.toLowerCase)
    }))
  }

  def findById(id: String): Future[Option[Api]] = Future.successful(apis.find(_.id == id))

  def apply(tenantId: String, blob: JsValue, group: String = "_"): Api = {
    val swagger = blob.as[JsObject]
    val basePath = (swagger \ "basePath").as[String]
    val host = (swagger \ "host").as[String]
    val description = (swagger \ "info" \ "description").as[String]
    val version = (swagger \ "info" \ "version").as[String]
    val title = (swagger \ "info" \ "title").as[String]
    val schemes = (swagger \ "schemes").as[JsArray].value.map(_.as[String])
    Api(
      id = UUID.randomUUID().toString,
      tenantId = tenantId,
      name = title,
      description = description,
      version = version,
      prodHosts = Seq(host),
      sandboxHosts = Seq(host),
      port = 80,
      group = group,
      root = basePath,
      schemes = schemes,
      swagger = swagger
    )
  }
}
