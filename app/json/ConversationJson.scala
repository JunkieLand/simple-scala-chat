package json

import play.api.libs.json.{JsValue, Json, Writes}
import services.domain_models.{Conversation, Message}
import utils.DateUtils

object ConversationJson {

  implicit val messageWriter = new Writes[Message] {
    override def writes(m: Message): JsValue = Json.obj(
      "sender" -> m.sender,
      "receiver" -> m.receiver,
      "created_at" -> DateUtils.isoDateTimeFormat.print(m.created_at),
      "text" -> m.text
    )
  }

  implicit val conversationWriter = new Writes[Conversation] {
    override def writes(c: Conversation): JsValue = Json.obj(
      "id" -> c.id,
      "created_at" -> DateUtils.isoDateTimeFormat.print(c.created_at),
      "message_count" -> c.message_count,
      "messages" -> c.messages
    )
  }

}
