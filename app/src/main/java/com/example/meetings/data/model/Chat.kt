package com.example.meetings.data.model

data class Chat(
    val uuid: String,
    val title: String,
    val messages: List<Message>,
    val createdAt: String,
    val meetingUUID: String
)

data class Message(
    val role: String,
    val content: String
)