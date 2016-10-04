package models

import java.util.UUID

import akka.stream.scaladsl.Source
import akka.util.ByteString
import controllers.HomeController._
import old.play.api.libs.ws._
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.libs.ws.StreamedBody
import play.api.mvc.{AnyContent, Request, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

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

  def save(): Future[Api] = Api.save(this)

  def json: JsValue = Json.obj(
    "id" -> this.id,
    "tenantId" -> this.tenantId,
    "name" -> this.name,
    "description" -> this.description,
    "version" -> this.version,
    "prodHosts" -> this.prodHosts,
    "sandboxHosts" -> this.sandboxHosts,
    "port" -> this.port,
    "group" -> this.group,
    "root" -> this.root,
    "schemes" -> this.schemes,
    "swagger" -> this.swagger
  )

  def call(group: String, path: String, hostF: Api => String, req: Request[AnyContent])(implicit ec: ExecutionContext): Future[Result] = {
    val api = this
    val scheme = api.schemes.headOption.getOrElse("http")
    val host = hostF(api)
    val url = s"$scheme://$host:${api.port}/$path"
    val queryString = req.queryString.toSeq.flatMap { case (key, values) => values.map(v => (key, v)) }
    val headers = req.headers.toSimpleMap.+(("Host", s"$host:${api.port}")).toSeq
    val sbody = StreamedBody(req.body.asRaw.flatMap(_.asBytes()).map(Source.single).getOrElse(Source.empty[ByteString])) // Stream IN
    Logger.debug(s"curl -X ${req.method.toUpperCase()} ${headers.map(h => s"--H '${h._1}: ${h._2}'").mkString(" ")} '$url?${queryString.map(h => s"${h._1}=${h._2}").mkString("&")}'")
    WS.url(url)
      .withRequestTimeout(Duration("5s")) // TODO : from API config
      .withMethod(req.method)
      .withQueryString(queryString:_*)
      .withHeaders(headers:_*)
      .withBody(sbody)
      .stream()
      .map { resp =>
        val headers = resp.headers.headers.toSeq.flatMap { case (key, values) => values.map(v => (key, v))}.filter(_._1 != "Content-Type")
        val contentType = resp.headers.headers.get("Content-Type").flatMap(_.headOption).getOrElse("application/pouet") // TODO : or not
        Status(resp.headers.status)
          .chunked(resp.body) // Stream OUT
          .withHeaders(headers:_*)
          .as(contentType)
      } recover {
      case error => InternalServerError(s"Api Mngr error : ${error.getMessage}") // TODO : based on accept type
    }
  }
}

object Api {

  private var apis = Seq.empty[Api]

  def findByTenantAndGroupAndPath(tenantId: String, group: String, path: String): Future[Option[Api]] = {
    Future.successful(apis.find(api => api.tenantId == tenantId && api.group == group && path.toLowerCase().startsWith(api.root.toLowerCase)))
  }

  def findById(id: String): Future[Option[Api]] = Future.successful(apis.find(_.id == id))

  def save(api: Api): Future[Api] = {
    Api.apis = Api.apis :+ api
    Future.successful(api)
  }

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
      port = 80, // TODO : split port
      group = group,
      root = basePath,
      schemes = schemes,
      swagger = swagger
    )
  }
}
