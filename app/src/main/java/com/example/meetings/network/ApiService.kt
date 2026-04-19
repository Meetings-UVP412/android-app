package com.example.meetings.network

import com.example.meetings.data.model.Chat
import com.example.meetings.data.model.Meeting
import com.example.meetings.data.model.SendMessageRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    @POST("api/meetings/create")
    @Headers("Content-Type: application/json")
    suspend fun createMeeting(@Body request: MeetingCreateRequest): Meeting

    @Multipart
    @POST("api/meetings/uploadFile")
    suspend fun uploadAudioFile(
        @Query("ord") ord: Int,
        @Query("isLast") isLast: Boolean,
        @Query("m-uid") meetingId: String,
        @Query("duration") duration: Int,
        @Part file: MultipartBody.Part
    ): Response<Unit>
}

data class MeetingCreateRequest(
    val name: String,
    val users: List<Int>,
    val authorId: Int,
    val link: String?,
    val comment: String?,
    val createdAt: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
)
