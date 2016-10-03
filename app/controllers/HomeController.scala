package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import old.play.api.libs.ws._
import old.play.api.libs.concurrent.Execution.Implicits._

object HomeController extends Controller {

  def index = Action.async {
    WS.url("http://freegeoip.net/json/").get.map { resp =>
      Ok(Json.prettyPrint(resp.json)).as("application/json")
    }
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
