package com.example.meetings.network

import com.example.meetings.data.model.Chat
import com.example.meetings.data.model.Meeting
import com.example.meetings.data.model.SendMessageRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
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

    @POST("chats/send")
    @Headers("Accept: application/json")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<Unit>
}