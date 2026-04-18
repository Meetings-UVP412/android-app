package com.example.meetings.data.repository

import com.example.meetings.data.model.Chat
import com.example.meetings.data.model.Meeting
import com.example.meetings.data.model.SendMessageRequest
import com.example.meetings.network.MeetingApi
import com.example.meetings.network.SseClient
import okhttp3.Call

class MeetingRepository {

    private val sseClient = SseClient(MeetingApi.BASE_URL)
    suspend fun fetchMeetings(): List<Meeting> {
        return MeetingApi.service.getMeetings()
    }

    suspend fun getMeetingById(meetingId: String): Meeting {
        return MeetingApi.service.getMeetingById(meetingId)
    }

    suspend fun getChatsByMeetingId(meetingId: String): List<Chat> {
        return MeetingApi.service.getChatsByMeetingId(meetingId)
    }

    suspend fun getChatHistory(chatId: String): Chat {
        return MeetingApi.service.getChatHistory(chatId)
    }

    fun sendMessageAndStream(
        request: SendMessageRequest,
        onChunkReceived: (String) -> Unit,
        onError: (Throwable) -> Unit,
        onComplete: () -> Unit
    ): Call {
        return sseClient.sendAndStream(request, onChunkReceived, onError, onComplete)
    }
}