package com.example.meetings.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.meetings.data.model.Chat
import com.example.meetings.data.model.Meeting
import com.example.meetings.data.model.SendMessageRequest
import com.example.meetings.network.MeetingApi
import com.example.meetings.network.MeetingCreateRequest
import com.example.meetings.network.SseClient
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

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


    suspend fun createMeeting(request: MeetingCreateRequest): Meeting {
        return try {
            val response = MeetingApi.service.createMeeting(request)
            Log.d("Network", "Meeting created: ${response.uuid}")
            response
        } catch (e: Exception) {
            Log.e("Network", "Create meeting error", e)
            throw e
        }
    }

    suspend fun uploadAudioFile(
        context: Context,
        meetingId: String,
        order: Int,
        isLast: Boolean,
        duration: Int,
        file: Uri
    ) {
        Log.d("Network", "Uploading audio to meeting: $meetingId, duration: $duration")
        try {
            val filePart = createMultipartBodyPart(context, file)
            val response = MeetingApi.service.uploadAudioFile(
                ord = order,
                isLast = isLast,
                meetingId = meetingId,
                duration = duration,
                file = filePart
            )
            Log.d("Network", "Upload successful: ${response.isSuccessful}")
        } catch (e: Exception) {
            Log.e("Network", "Upload failed", e)
            throw e
        }
    }

    private fun createMultipartBodyPart(context: Context, fileUri: Uri): MultipartBody.Part {
        val fileName = getFileName(context, fileUri) ?: "audio_file"

        val inputStream = context.contentResolver.openInputStream(fileUri)
        val file = File.createTempFile("audio", ".tmp", context.cacheDir).apply {
            outputStream().use { out ->
                inputStream?.copyTo(out)
                out.flush()
            }
        }

        val requestFile = RequestBody.create(
            "audio/*".toMediaTypeOrNull(),
            file
        )
        return MultipartBody.Part.createFormData("file", fileName, requestFile)
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            }
        }
        return null
    }
}