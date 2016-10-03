package controllers

import akka.stream.scaladsl.Source
import akka.util.ByteString
import models.{Api, Tenant}
import old.play.api.libs.concurrent.Execution.Implicits._
import old.play.api.libs.ws._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.StreamedBody
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration.Duration

object HomeController extends Controller {

  def index = Action.async {
    WS.url("http://freegeoip.net/json/").get.map { resp =>
      Ok(Json.prettyPrint(resp.json)).as("application/json")
    }
  }

  def createApiFromSwaggerUrl(urlOpt: Option[String]) = Action.async { req =>
    urlOpt match {
      case None => Future.successful(NotFound("Not Found"))
      case Some(url) => {
        WS.url(url).get().map { resp =>
          val swagger = (resp.json.as[JsObject] \ "swagger").as[String]
          swagger match {
            case "2.0" =>
              val api = Api(Tenant.tenants.head.id, resp.json).save()
              Ok(Json.prettyPrint(resp.json)).as("application/json")
            case _ => InternalServerError(s"Swagger version $swagger not supported")
          }
        }
      }
    }
  }

  def callSandboxApi(tenant: String, group: String, path: String) = Action.async { req =>
    callApi(tenant, group, path, _.prodHosts.head, req)
  }
  def callProdApi(tenant: String, group: String, path: String) = Action.async { req =>
    callApi(tenant, group, path, _.prodHosts.head, req)
  }

  private def callApi(tenantRoot: String, group: String, path: String, hostF: Api => String, req: Request[AnyContent]): Future[Result] = {
    Tenant.findByRoot(tenantRoot).flatMap {
      case None => Future.successful(NotFound("Tenant not found"))  // TODO : based on accept type
      case Some(tenant) => {
        Api.findByTenantAndGroupAndPath(tenant.id, group, "/" + path).flatMap {
          case None => Future.successful(NotFound("API not found"))  // TODO : based on accept type
          case Some(api) => {
            val scheme = api.schemes.headOption.getOrElse("http")
            val host = hostF(api)
            val url = s"$scheme://$host:${api.port}/$path"
            val queryString = req.queryString.toSeq.flatMap { case (key, values) => values.map(v => (key, v)) }
            val headers = req.headers.toSimpleMap.+(("Host", s"$host:${api.port}")).toSeq
            val sbody = StreamedBody(req.body.asRaw.flatMap(_.asBytes()).map(Source.single).getOrElse(Source.empty[ByteString]))
            Logger.debug(s"curl -X ${req.method.toUpperCase()} ${headers.map(h => s"--H '${h._1}: ${h._2}'").mkString(" ")} '$url?${queryString.map(h => s"${h._1}=${h._2}").mkString("&")}'")
            WS.url(url)
              .withRequestTimeout(Duration("5s"))
              .withMethod(req.method)
              .withQueryString(queryString:_*)
              .withHeaders(headers:_*)
              .withBody(sbody)
              .stream()
              .map { resp =>
                val responseHeaders = resp.headers.headers.toSeq.flatMap { case (key, values) => values.map(v => (key, v))}
                Status(resp.headers.status)
                  .chunked(resp.body)
                  .withHeaders(responseHeaders:_*)
              } recover {
              case error => InternalServerError(s"Api Mngr error : ${error.getMessage}") // TODO : based on accept type
            }
          }
        }
      }
    }
  }

  def notImplemented = Action {
    NotImplemented("Not implemented yet ...")
  }

  def notImplemented1(path: String) = notImplemented

  def notImplemented3(a: String, b: String, c: String) = notImplemented
}
