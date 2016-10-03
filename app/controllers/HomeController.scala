package controllers

import javax.inject._
import play.api._
import play.api.mvc._

object HomeController extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def none = Action {
    ???
  }

  def none1(path: String) = Action {
    ???
  }

  def none1(a: String, b: String, c: String) = Action {
    ???
  }
}
