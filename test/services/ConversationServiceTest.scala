package services

import anorm.JodaParameterMetaData._
import anorm._
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import play.api.db.{Database, Databases}
import play.api.db.evolutions.Evolutions
import services.domain_models.Message

class ConversationServiceTest extends FunSuite with Matchers with BeforeAndAfter {

  implicit var db: Database = _

  before {
    db = Databases.inMemory(
      name = "default",
      urlOptions = Map(
        "MODE" -> "MYSQL"
      ),
      config = Map(
        "logStatements" -> true
      )
    )

    Evolutions.applyEvolutions(db)
  }

  after {
    db.shutdown()
  }


  test("User should be allowed to see conversation if is sender") {
    // -- Given
    val userId = 42L
    val conversationId = 69L

    insertUser(userId)
    insertUser(666)
    insertConversation(conversationId)
    insertMessage(messageId = 1, conversationId = conversationId, senderId = userId, receiverId = 666)

    // -- When
    val isAllowed = ConversationService.isUserAllowedToAccessConversation(userId, conversationId)

    // -- Then
    isAllowed should be(true)
  }

  test("User should be allowed to see conversation if is receiver") {
    // -- Given
    val userId = 42L
    val conversationId = 69L

    insertUser(userId)
    insertUser(666)
    insertConversation(conversationId)
    insertMessage(messageId = 1, conversationId = conversationId, senderId = 666, receiverId = userId)

    // -- When
    val isAllowed = ConversationService.isUserAllowedToAccessConversation(userId, conversationId)

    // -- Then
    isAllowed should be(true)
  }

  test("User should not be allowed to see conversation if is not sender nor receiver") {
    // -- Given
    val userId = 42L
    val conversationId = 69L

    insertUser(userId)
    insertUser(666)
    insertUser(777)
    insertConversation(conversationId)
    insertMessage(messageId = 1, conversationId = conversationId, senderId = 666, receiverId = 777)

    // -- When
    val isAllowed = ConversationService.isUserAllowedToAccessConversation(userId, conversationId)

    // -- Then
    isAllowed should be(false)
  }

  test("Should return conversation if user is allowed") {
    // -- Given
    val userId1 = 42L
    val username1 = "Sender"
    val userId2 = 69L
    val username2 = "Receiver"
    val conversationId = 69L
    val conversationCreatedAt = new DateTime(2099, 11, 29, 22, 58, 58, 100)
    val messageCount = 2L
    val messageCreatedAt1 = new DateTime(2099, 11, 30, 22, 58, 58, 100)
    val text1 = "Text 1"
    val messageCreatedAt2 = new DateTime(2099, 12, 31, 22, 58, 58, 100)
    val text2 = "Text 2"

    insertUser(userId1, username1)
    insertUser(userId2, username2)
    insertConversation(conversationId, conversationCreatedAt, messageCount)
    insertMessage(
      messageId = 1,
      conversationId = conversationId,
      senderId = userId1,
      receiverId = userId2,
      createdAt = messageCreatedAt1,
      text = text1
    )
    insertMessage(
      messageId = 2,
      conversationId = conversationId,
      senderId = userId2,
      receiverId = userId1,
      createdAt = messageCreatedAt2,
      text = text2
    )

    // -- When
    val conversation = ConversationService.getConversation(userId1, conversationId).get

    // -- Then
    conversation.id should be(conversationId)
    conversation.created_at should be(conversationCreatedAt)
    conversation.message_count should be(messageCount)
    conversation.messages.size should be(messageCount)
    conversation.messages should contain theSameElementsAs Seq(
      Message(sender = username1, receiver = username2, created_at = messageCreatedAt1, text = text1),
      Message(sender = username2, receiver = username1, created_at = messageCreatedAt2, text = text2)
    )
    conversation.id should be(conversationId)
  }

  test("Should not return conversation if user is not allowed") {
    // -- Given
    val notAllowedUserId = 666L
    val userId1 = 42L
    val username1 = "Sender"
    val userId2 = 69L
    val username2 = "Receiver"
    val conversationId = 69L
    val conversationCreatedAt = new DateTime(2099, 11, 29, 22, 58, 58, 100)
    val messageCount = 2L
    val messageCreatedAt1 = new DateTime(2099, 11, 30, 22, 58, 58, 100)
    val text1 = "Text 1"
    val messageCreatedAt2 = new DateTime(2099, 12, 31, 22, 58, 58, 100)
    val text2 = "Text 2"

    insertUser(userId1, username1)
    insertUser(userId2, username2)
    insertConversation(conversationId, conversationCreatedAt, messageCount)
    insertMessage(
      messageId = 1,
      conversationId = conversationId,
      senderId = userId1,
      receiverId = userId2,
      createdAt = messageCreatedAt1,
      text = text1
    )
    insertMessage(
      messageId = 2,
      conversationId = conversationId,
      senderId = userId2,
      receiverId = userId1,
      createdAt = messageCreatedAt2,
      text = text2
    )

    // -- When
    val conversation = ConversationService.getConversation(notAllowedUserId, conversationId)

    // -- Then
    conversation.isDefined should be(false)
  }

  test("Should create new conversation") {
    // -- Given
    val userId = 42L

    insertUser(userId)

    // -- When
    val conversationId = ConversationService.createNewConversation(userId).get

    // -- Then
    conversationId should be(1L)
  }

  test("Should be allowed to add message conversation if no message yet") {
    // -- Given
    val userId1 = 42L
    val userId2 = 69L
    val conversationId = 666L

    insertUser(userId1)
    insertUser(userId2)
    insertConversation(conversationId)

    // -- When
    val isAllowed = ConversationService.isAllowedToAddMessage(conversationId, userId1, userId2)

    // -- Then
    isAllowed should be(true)
  }

  test("Should be allowed to add message conversation if sender and receiver are in conversation") {
    // -- Given
    val userId1 = 42L
    val userId2 = 69L
    val conversationId = 666L

    insertUser(userId1)
    insertUser(userId2)
    insertConversation(conversationId)
    insertMessage(messageId = 1, conversationId = conversationId, senderId = userId1, receiverId = userId2)

    // -- When
    val isAllowed = ConversationService.isAllowedToAddMessage(conversationId, userId1, userId2)

    // -- Then
    isAllowed should be(true)
  }

  test("Should not be allowed to add message conversation if sender not in conversation") {
    // -- Given
    val userId1 = 42L
    val userId2 = 69L
    val notAllowedUserId = 777L
    val conversationId = 666L

    insertUser(userId1)
    insertUser(userId2)
    insertConversation(conversationId)
    insertMessage(messageId = 1, conversationId = conversationId, senderId = userId1, receiverId = userId2)

    // -- When
    val isAllowed = ConversationService.isAllowedToAddMessage(conversationId, notAllowedUserId, userId2)

    // -- Then
    isAllowed should be(false)
  }

  test("Should not be allowed to add message conversation if receiver not in conversation") {
    // -- Given
    val userId1 = 42L
    val userId2 = 69L
    val userNotAllowedId = 777L
    val conversationId = 666L

    insertUser(userId1)
    insertUser(userId2)
    insertConversation(conversationId)
    insertMessage(messageId = 1, conversationId = conversationId, senderId = userId1, receiverId = userId2)

    // -- When
    val isAllowed = ConversationService.isAllowedToAddMessage(conversationId, userId1, userNotAllowedId)

    // -- Then
    isAllowed should be(false)
  }

  test("Should insert message if allowed") {
    // -- Given
    val senderId = 42L
    val senderUsername = "Sender"
    val receiverId = 69L
    val receiverUsername = "Receiver"
    val conversationId = 69L
    val messageCount = 1L
    val text = "New message"
    val now = DateTime.now()

    insertUser(senderId, senderUsername)
    insertUser(receiverId, receiverUsername)
    insertConversation(conversationId = conversationId, messageCount = messageCount)
    insertMessage(
      messageId = 1,
      conversationId = conversationId,
      senderId = senderId,
      receiverId = receiverId
    )

    // -- When
    val isSucceful = ConversationService.addMessage(conversationId, senderId, receiverId, text)

    // -- Then
    isSucceful should be(true)
    val conversation = ConversationService.getConversation(senderId, conversationId).get
    conversation.message_count should be(messageCount + 1)
    conversation.messages.size should be(messageCount + 1)
    val newMessage = conversation.messages.find(_.text == "New message").get
    newMessage.text should be(text)
    newMessage.sender should be(senderUsername)
    newMessage.receiver should be(receiverUsername)
    newMessage.created_at.isAfter(now) should be(true)
  }

  test("Should not insert message if not allowed") {
    // -- Given
    val notAllowedUserId = 666L
    val senderId = 42L
    val senderUsername = "Sender"
    val receiverId = 69L
    val receiverUsername = "Receiver"
    val conversationId = 69L
    val messageCount = 1L
    val text = "New message"
    val now = DateTime.now()

    insertUser(senderId, senderUsername)
    insertUser(receiverId, receiverUsername)
    insertConversation(conversationId = conversationId, messageCount = messageCount)
    insertMessage(
      messageId = 1,
      conversationId = conversationId,
      senderId = senderId,
      receiverId = receiverId
    )

    // -- When
    val isSucceful = ConversationService.addMessage(conversationId, notAllowedUserId, receiverId, text)

    // -- Then
    isSucceful should be(false)
    val conversation = ConversationService.getConversation(senderId, conversationId).get
    conversation.message_count should be(messageCount)
    conversation.messages.size should be(messageCount)
  }

  private def insertUser(userId: Long, username: String = "") = {
    db.withConnection { implicit conn =>
      SQL(
        """
          | INSERT INTO user(id, username, full_name, age)
          | VALUES ({userId}, {username}, {full_name}, 99)
        """.stripMargin
      ).on(
        "userId" -> userId,
        "username" -> username,
        "full_name" -> username
      ).executeInsert()
    }
  }

  private def insertConversation(conversationId: Long,
                                 createdAt: DateTime = DateTime.now(),
                                 messageCount: Long = 0) = {
    db.withConnection { implicit conn =>
      SQL(
        """
          | INSERT INTO conversation(id, create_at, message_count)
          | VALUES ({conversationId}, {createdAt}, {messageCount})
        """.stripMargin
      ).on(
        "conversationId" -> conversationId,
        "createdAt" -> createdAt,
        "messageCount" -> messageCount
      ).executeInsert()
    }
  }

  private def insertMessage(messageId: Long,
                            conversationId: Long,
                            createdAt: DateTime = DateTime.now(),
                            senderId: Long,
                            receiverId: Long,
                            text: String = "") = {
    db.withTransaction { implicit conn =>
      SQL(
        """
          | INSERT INTO message(id, conversation_id, created_at, sender, receiver, text)
          | VALUES ({messageId}, {conversationId}, {createdAt}, {senderId}, {receiverId}, {text})
        """.stripMargin
      ).on(
        "messageId" -> messageId,
        "conversationId" -> conversationId,
        "createdAt" -> createdAt,
        "senderId" -> senderId,
        "receiverId" -> receiverId,
        "text" -> text
      ).executeInsert()
    }
  }


}
