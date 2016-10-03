package controllers

import javax.inject._
import play.api._
import play.api.mvc._

object HomeController extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def none = Action {
    Ok("")
  }

  def none1(path: String) = Action {
    Ok("")
  }

  def none3(a: String, b: String, c: String) = Action {
    Ok("")
  }
}
