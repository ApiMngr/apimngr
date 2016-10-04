package utils

import models.Tenant
import play.api.mvc.{ActionBuilder, Request, Result, Results}

import scala.concurrent.Future

object PublicAction extends ActionBuilder[Request] {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    block(request)
  }
}

case class AdminCallCtx[A]()

object AdminAction extends ActionBuilder[AdminCallCtx] {
  override def invokeBlock[A](request: Request[A], block: (AdminCallCtx[A]) => Future[Result]): Future[Result] = {
    // TODO : check if admin
    ???
  }
}

case class AdminApiCallCtx[A](tenant: Tenant, request: Request[A])

object AdminApiAction extends ActionBuilder[AdminApiCallCtx] {

  implicit val ec = old.play.Env.httpRequestExecContext

  override def invokeBlock[A](request: Request[A], block: (AdminApiCallCtx[A]) => Future[Result]): Future[Result] = {
    // TODO : check if admin
    val tenantRoot = request.path.tail.split("/")(0)
    Tenant.findByRoot(tenantRoot).flatMap {
      case None => Future.successful(Results.NotFound(s"Tenant $tenantRoot not found"))  // TODO : based on accept type
      case Some(tenant) => block(AdminApiCallCtx(tenant, request))
    }
  }
}

case class DeveloperCallCtx[A](tenant: Tenant, request: Request[A])

object DeveloperAction extends ActionBuilder[DeveloperCallCtx] {

  implicit val ec = old.play.Env.httpRequestExecContext

  override def invokeBlock[A](request: Request[A], block: (DeveloperCallCtx[A]) => Future[Result]): Future[Result] = {
    // TODO : check if dev
    val tenantRoot = request.path.tail.split("/")(0)
    Tenant.findByRoot(tenantRoot).flatMap {
      case None => Future.successful(Results.NotFound(s"Tenant $tenantRoot not found"))  // TODO : based on accept type
      case Some(tenant) => block(DeveloperCallCtx(tenant, request))
    }
  }
}