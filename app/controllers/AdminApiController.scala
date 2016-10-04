package controllers

import models.Api
import old.play.Env
import old.play.api.libs.ws._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Controller
import utils.AdminApiAction

import scala.concurrent.Future

object AdminApiController extends Controller {

  def createApiFromSwaggerUrl(tenant: String) = AdminApiAction.async(parse.json) { ctx =>

    implicit val ec = Env.httpCallsExecContext

    (ctx.request.body \ "url").asOpt[String] match {
      case None => Future.successful(NotFound("Not Found"))
      case Some(url) => {
        WS.url(url).get().flatMap { resp =>
          val swagger = (resp.json.as[JsObject] \ "swagger").as[String]
          swagger match {
            case "2.0" => Api(ctx.tenant.id, resp.json).save().map { api =>
              Ok(Json.prettyPrint(api.json)).as("application/json")
            }
            case _ =>
              Future.successful(InternalServerError(s"Swagger version $swagger not supported"))
          }
        }
      }
    }
  }
}
