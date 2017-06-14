package controllers

import javax.inject.Inject

import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.ConversationService

class ConversationController @Inject()(implicit db: Database) extends Controller {

  import json.ConversationJson._

  def createConversation(userId: Long) = Action { implicit request =>
    ConversationService
      .createNewConversation(userId)
      .map { conversationId =>
        Created(Json.toJson(conversationId))
      }.getOrElse(BadRequest)
  }

  def getConversation(userId: Long, conversationId: Long) = Action { implicit request =>
    if (ConversationService.isUserAllowedToAccessConversation(userId, conversationId)) {
      ConversationService
        .getConversation(userId, conversationId)
        .map(conversation => Ok(Json.toJson(conversation)))
        .getOrElse(BadRequest)
    } else {
      Unauthorized
    }
  }

  def addMessage(conversationId: Long) = Action { implicit request =>
    val params = addMessageForm.bindFromRequest().get
    if (ConversationService.isAllowedToAddMessage(conversationId, params.sender_id, params.receiver_id)) {
      val isSuccessful = ConversationService
        .addMessage(
          conversationId = conversationId,
          senderId = params.sender_id,
          receiverId = params.receiver_id,
          text = params.text
        )

      if (isSuccessful) Created else BadRequest
    } else {
      Unauthorized
    }
  }

  val addMessageForm = Form(
    mapping(
      "sender_id" -> longNumber,
      "receiver_id" -> longNumber,
      "text" -> text
    )(AddMessageParam.apply)(AddMessageParam.unapply)
  )

  case class AddMessageParam(sender_id: Long, receiver_id: Long, text: String)

}
