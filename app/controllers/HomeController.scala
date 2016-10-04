package controllers

import play.api.mvc._
import utils.PublicAction

object HomeController extends Controller {

  def index = PublicAction {
    // WS.url("http://freegeoip.net/json/").get.map { resp =>
    //   Ok(Json.prettyPrint(resp.json)).as("application/json")
    // }
    Ok("Api Mngr")
  }

  def notImplemented = PublicAction {
    NotImplemented("Not implemented yet ...")
  }

  def notImplemented1(path: String) = notImplemented

  def notImplemented3(a: String, b: String, c: String) = notImplemented
}
