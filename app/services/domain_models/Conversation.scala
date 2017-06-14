package services.domain_models

import org.joda.time.DateTime

case class Message(
  sender: String,
  receiver: String,
  created_at: DateTime,
  text: String
)

case class Conversation(
  id: Long,
  created_at: DateTime,
  message_count: Long,
  messages: Seq[Message]
)

case class User(
  username: String,
  fullName: String,
  age: Long
)