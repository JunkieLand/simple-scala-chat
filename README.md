# Simple Scala Chat

## Architecture

This application consists only of an API allowing to create users, conversations, and to post messages.

#### API

The API is coded in Scala and rely on the Play2! Framework.

#### Database

For this simple exercise, I have used a H2 in-memory database set to work with the MySQL SQL syntax. The database schema
will be automaticaly set when starting the app. The database schema can be found in
`conf/evolutions/default/1.sql`.

## How to run

To run this project, simply run :

    sbt run

A web server will be started on port 9000 by default.

## Available APIs

Before posting messages, you need to create at least one user (yes he can chat with himself), and a conversation.

#### Create a user

  * HTTP verb : POST
  * URL : /user
  * Form parameters :
    * username (String)
    * full_name (String)
    * age (Long)
  * Returns : the created user id (Long)

#### Create a conversation

  * HTTP verb : POST
  * URL : /user/{userId}/conversation
  * Returns : the created conversation id (Long)

#### Post a message

  * HTTP verb : POST
  * URL : /conversation/{conversationId}/message
  * Form parameters :
    * sender_id (Long) : the user id of the message sender
    * receiver_id (Long) : the user id of the message receiver
    * text (String) : the content of the message

#### Get a conversation

  * HTTP verb : GET
  * URL : /user/{userId}/conversation/{conversationId}
  * Returns : a Json document of the conversation
  
The Json structure is :

    {
      "id": 1,
      "created_at": "2017-06-14T10:57:16.095+02:00",
      "message_count": 2,
      "messages": [{
        "sender": "Donald",
        "receiver": "Mickey",
        "created_at": "2017-06-14T11:11:46.058+02:00",
        "text": "I can talk !"
      }, {
        "sender": "Mickey",
        "receiver": "Donald",
        "created_at": "2017-06-14T11:12:09.836+02:00",
        "text": "Hey ! I can talk too !"
      }]
    }

Note : the user must be part of the conversation to be allowed to see it.
