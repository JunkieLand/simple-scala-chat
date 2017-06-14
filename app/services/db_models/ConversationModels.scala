package services.db_models

import org.joda.time.DateTime

case class RawConversation(
  conversation_id: Long,
  conversation_created_at: DateTime,
  message_count: Long,
  sender: String,
  receiver: String,
  message_created_at: DateTime,
  text: String
)
