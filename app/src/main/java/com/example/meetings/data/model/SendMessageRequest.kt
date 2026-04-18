package com.example.meetings.data.model

data class SendMessageRequest(
    val meetingUUID: String,
    val chatUUID: String,
    val message: Message
) { }