package controllers

import models.{Api, Tenant}
import old.play.Env
import play.api.libs.json.Json
import play.api.mvc._
import utils.{DeveloperAction, PublicAction}

object HomeController extends Controller {

  def index = PublicAction.async {

    implicit val ec = Env.httpRequestExecContext

    Tenant.all().map { tenants =>
      Ok(views.html.index(tenants))
    }
  }

  def apis(tenant: String) = DeveloperAction.async { ctx =>

    implicit val ec = Env.httpRequestExecContext

    Api.all().map { apis =>
      Ok(views.html.apis(ctx.tenant, apis))
    }
  }

  def api(tenant: String, id: String) = DeveloperAction.async { ctx =>

    implicit val ec = Env.httpRequestExecContext

    for {
      apis <- Api.all()
      apiOpt  <- Api.findById(id)
    } yield apiOpt.map(api => Ok(views.html.api(ctx.tenant, apis, api))).getOrElse(NotFound("Api not found"))
  }

  def apiDescriptor(tenant: String, id: String) = DeveloperAction.async { ctx =>

    implicit val ec = Env.httpRequestExecContext

    Api.findById(id).map {
      case None => NotFound(Json.obj("error" -> "not found"))
      case Some(api) => Ok(Json.prettyPrint(api.translatedDescriptor(tenant))).as("application/json")
    }
  }

  def notImplemented1(path: String) = notImplemented
  def notImplemented3(a: String, b: String, c: String) = notImplemented
  def notImplemented = PublicAction {
    NotImplemented("Not implemented yet ...")
  }
}

// WS.url("http://freegeoip.net/json/").get.map { resp =>
//   Ok(Json.prettyPrint(resp.json)).as("application/json")
// }
