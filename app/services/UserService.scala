package services

import anorm._
import play.api.db.Database
import services.db_models.RawConversation

object UserService {

  val conversationParser = Macro.namedParser[RawConversation]

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
