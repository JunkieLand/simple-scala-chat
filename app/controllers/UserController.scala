package controllers

import javax.inject.Inject

import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.domain_models.User
import services.{ConversationService, UserService}

class UserController @Inject()(implicit db: Database) extends Controller {

  def createUser() = Action { implicit request =>
    val params = userForm.bindFromRequest().get
    UserService
      .createUser(params.username, params.full_name, params.age)
      .map { userId =>
        Created(Json.toJson(userId))
      }
      .getOrElse(BadRequest)
  }

  val userForm = Form(
    mapping(
      "username" -> text,
      "full_name" -> text,
      "age" -> longNumber
    )(CreateUserParam.apply)(CreateUserParam.unapply)
  )

  case class CreateUserParam(username: String, full_name: String, age: Long)

}
