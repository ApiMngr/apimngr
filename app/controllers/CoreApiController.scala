package controllers

import models.{Api, Tenant}
import old.play.Env
import play.api.mvc._
import utils.DeveloperAction

import scala.concurrent.Future

object CoreApiController extends Controller {

  def callSandboxApi(tenant: String, group: String, path: String) = DeveloperAction.async { ctx =>
    callApi(ctx.tenant, group, path, _.prodHosts.head, ctx.request)
  }

  def callProdApi(tenant: String, group: String, path: String) = DeveloperAction.async { ctx =>
    callApi(ctx.tenant, group, path, _.prodHosts.head, ctx.request)
  }

  private def callApi(tenant: Tenant, group: String, path: String, hostF: Api => String, req: Request[AnyContent]): Future[Result] = {
    implicit val ec = Env.dataStoreExecContext
    Api.findByTenantAndGroupAndPath(tenant.id, group, s"/$path").flatMap {
      case None => Future.successful(NotFound("API not found"))  // TODO : based on accept type
      case Some(api) => api.call(group, path, hostF, req)(Env.httpCallsExecContext)
    }
  }
}
