# --- !Ups

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE user (
  id int(11) NOT NULL AUTO_INCREMENT,
  username varchar(50) NOT NULL,
  full_name varchar(200) NOT NULL,
  age int(11) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;


CREATE TABLE conversation (
  id int(11) NOT NULL AUTO_INCREMENT,
  creator_id int(11) DEFAULT NULL,
  create_at datetime DEFAULT NULL,
  message_count int(11) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY user_id (creator_id),
  CONSTRAINT creatorfk FOREIGN KEY (creator_id) REFERENCES user (id) ON DELETE CASCADE ON
  UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;


CREATE TABLE message (
  id int(11) NOT NULL AUTO_INCREMENT,
  text text,
  created_at datetime DEFAULT NULL,
  sender int(11) DEFAULT NULL,
  receiver int(11) DEFAULT NULL,
  conversation_id int(11) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY sender (sender),
  KEY conversation_id (conversation_id),
  KEY receiver (receiver),
  CONSTRAINT receiverfk FOREIGN KEY (receiver) REFERENCES user (id) ON DELETE CASCADE ON
  UPDATE CASCADE,
  CONSTRAINT conversationfk FOREIGN KEY (conversation_id) REFERENCES conversation (id) ON
  DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT senderfk FOREIGN KEY (sender) REFERENCES user (id) ON DELETE CASCADE ON
  UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;


SET FOREIGN_KEY_CHECKS = 1;


# --- !Downs

DROP TABLE IF EXISTS conversation;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS user;
