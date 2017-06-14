package services.mappings

import services.db_models.RawConversation
import services.domain_models.{Conversation, Message}

object ConversationMapping{

  def toConversation(rawConversations: Seq[RawConversation]): Option[Conversation] = {
    rawConversations
      .headOption
      .map { firstRawConversation =>
        Conversation(
          id = firstRawConversation.conversation_id,
          created_at = firstRawConversation.conversation_created_at,
          message_count = firstRawConversation.message_count,
          messages = rawConversations.map(toMessage)
        )
      }
  }

  def toMessage(rawConversation: RawConversation): Message = Message(
    sender = rawConversation.sender,
    receiver = rawConversation.receiver,
    created_at = rawConversation.message_created_at,
    text = rawConversation.text
  )

}
