package com.example.meetings.network

import com.example.meetings.data.model.Chat
import com.example.meetings.data.model.Meeting
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("api/meetings")
    suspend fun getMeetings(): List<Meeting>

    @GET("api/meetings/{uuid}")
    suspend fun getMeetingById(@Path("uuid") uuid: String): Meeting

    @GET("chats/{meetingId}")
    suspend fun getChatsByMeetingId(@Path("meetingId") meetingId: String): List<Chat>

    @GET("chats/history/{chatId}")
    suspend fun getChatHistory(@Path("chatId") chatId: String): Chat
}