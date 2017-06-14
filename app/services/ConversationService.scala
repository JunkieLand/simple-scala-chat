package services

import anorm.JodaParameterMetaData._
import anorm._
import org.joda.time.DateTime
import play.api.db.Database
import services.db_models.RawConversation
import services.domain_models.Conversation
import services.mappings.ConversationMapping

object ConversationService {

  val conversationParser = Macro.namedParser[RawConversation]

  def isUserAllowedToAccessConversation(userId: Long, conversationId: Long)(implicit db: Database): Boolean = {
    db.withConnection { implicit conn =>
      val isAllowed: List[Long] = SQL(
        """
          | SELECT m.sender
          | FROM message m
          | WHERE (m.sender = {userId} OR m.receiver = {userId}) AND m.conversation_id = {conversationId}
          | LIMIT 1
        """.stripMargin
      ).on(
        "userId" -> userId.toString,
        "conversationId" -> conversationId.toString
      ).as(SqlParser.long("sender").*)

      isAllowed.nonEmpty
    }
  }

  def getConversation(userId: Long, conversationId: Long)(implicit db: Database): Option[Conversation] = {
    if (isUserAllowedToAccessConversation(userId, conversationId)) {
      val rawConversations: Seq[RawConversation] = db.withConnection { implicit conn =>
        SQL(
          """
            | SELECT c.id conversation_id, c.create_at conversation_created_at, c.message_count,
            |        us.username sender, ur.username receiver, m.created_at message_created_at, m.text
            | FROM conversation c
            | INNER JOIN message m
            | ON c.id = m.conversation_id
            | INNER JOIN user us
            | ON us.id = m.sender
            | INNER JOIN user ur
            | ON ur.id = m.receiver
            | WHERE c.id = {conversationId}
          """.stripMargin
        ).on(
          "conversationId" -> conversationId
        ).as(conversationParser.*)
      }

      ConversationMapping.toConversation(rawConversations)
    } else {
      None
    }
  }

  def createNewConversation(userId: Long)(implicit db: Database): Option[Long] = {
    db.withConnection { implicit conn =>
      SQL(
        """
          | INSERT INTO conversation(creator_id, create_at, message_count)
          | VALUES ({creatorId}, {createdAt}, 0)
        """.stripMargin
      ).on(
        "creatorId" -> userId,
        "createdAt" -> DateTime.now()
      ).executeInsert()
    }
  }

  def isAllowedToAddMessage(conversationId: Long,
                            senderId: Long,
                            receiverId: Long)(implicit db: Database): Boolean = {
    db.withConnection { implicit conn =>
      val hasMessages = SQL(
        """
          | SELECT m.id
          | FROM message m
          | INNER JOIN conversation c
          | ON m.conversation_id = c.id
          | WHERE c.id = {conversationId}
          | LIMIT 1
        """.stripMargin
      ).on(
        "conversationId" -> conversationId
      ).as(SqlParser.long("id").*)

      lazy val isParticipant: List[Long] = SQL(
        """
          | SELECT m.id
          | FROM message m
          | INNER JOIN conversation c
          | ON m.conversation_id = c.id
          | WHERE c.id = {conversationId}
          | AND (
          |   (m.sender = {senderId} AND m.receiver = {receiverId})
          |   OR
          |   (m.sender = {receiverId} AND m.receiver = {senderId})
          | )
          | LIMIT 1
        """.stripMargin
      ).on(
        "conversationId" -> conversationId,
        "senderId" -> senderId,
        "receiverId" -> receiverId
      ).as(SqlParser.long("id").*)

      hasMessages.isEmpty || isParticipant.nonEmpty
    }
  }

  def addMessage(conversationId: Long,
                 senderId: Long,
                 receiverId: Long,
                 text: String)(implicit db: Database): Boolean = {
    if (isAllowedToAddMessage(conversationId, senderId, receiverId)) {
      db.withTransaction { implicit conn =>
        val messageId: Option[Long] = SQL(
          """
            | INSERT INTO message(text, created_at, sender, receiver, conversation_id)
            | VALUES ({text}, {created_at}, {senderId}, {receiverId}, {conversationId})
          """.stripMargin
        ).on(
          "text" -> text,
          "created_at" -> DateTime.now(),
          "senderId" -> senderId,
          "receiverId" -> receiverId,
          "conversationId" -> conversationId
        ).executeInsert()

        val updatedRowCount = SQL(
          """
            | UPDATE conversation
            | SET message_count = (
            |   SELECT message_count
            |   FROM conversation
            |   WHERE id = {conversationId}
            | ) + 1
            | WHERE id = {conversationId}
          """.stripMargin
        ).on(
          "conversationId" -> conversationId
        ).executeUpdate()

        messageId.isDefined && updatedRowCount == 1
      }
    } else {
      false
    }
  }

  def createUser(username: String, fullName: String, age: Long)(implicit db: Database): Option[Long] = {
    db.withConnection { implicit conn =>
      SQL(
        """
          | INSERT INTO user(username, full_name, age)
          | VALUES ({userName}, {fullName}, {age})
        """.stripMargin
      ).on(
        "userName" -> username,
        "fullName" -> fullName,
        "age" -> age
      ).executeInsert()
    }
  }

}
