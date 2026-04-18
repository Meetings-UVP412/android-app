package com.example.meetings.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.meetings.data.model.SendMessageRequest
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import java.io.IOException

class SseClient(private val baseUrl: String) {

    private val client = OkHttpClient()
    private var currentDataBuffer = ""

    fun sendAndStream(
        request: SendMessageRequest,
        onChunkReceived: (String) -> Unit,
        onError: (Throwable) -> Unit,
        onComplete: () -> Unit
    ): Call {
        val json = Gson().toJson(request)
        val body = json.toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url("$baseUrl/chats/send")
            .post(body)
            .addHeader("Accept", "text/event-stream")
            .addHeader("Content-Type", "application/json")
            .build()

        val call = client.newCall(httpRequest)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("SseClient", "SSE connection opened")

                if (!response.isSuccessful) {
                    val error = IOException("HTTP ${response.code}: ${response.message}")
                    Log.e("SseClient", "HTTP error", error)
                    onError(error)
                    return
                }

                val source = response.body?.source()
                val buffer = Buffer()

                try {
                    var expectingData = false
                    while (source?.read(buffer, 8192L) != -1L) {
                        val rawData = buffer.readString(Charsets.UTF_8)
                        Log.d("SseClient", "Raw data received: [$rawData]")

                        rawData.split("\n").forEach { line ->
                            Log.d("SseClient", "Processing line: [$line]")

                            if (line == "data:") {
                                expectingData = true
                            } else if (expectingData && line.isNotEmpty()) {
                                Log.d("SseClient", "Valid chunk extracted: [$line]")
                                Handler(Looper.getMainLooper()).post {
                                    onChunkReceived(line)
                                }
                                expectingData = false
                            } else if (line.startsWith("data:") && line.length > 5) {
                                val chunk = line.substring(5).trim()
                                if (chunk.isNotEmpty()) {
                                    Log.d("SseClient", "Valid chunk (inline): [$chunk]")
                                    Handler(Looper.getMainLooper()).post {
                                        onChunkReceived(chunk)
                                    }
                                }
                            }
                        }
                        buffer.clear()
                    }
                    Log.d("SseClient", "SSE stream completed")
                    Handler(Looper.getMainLooper()).post { onComplete() }
                } catch (e: Exception) {
                    Log.e("SseClient", "Error in SSE stream", e)
                    Handler(Looper.getMainLooper()).post { onError(e) }
                } finally {
                    response.close()
                    Log.d("SseClient", "CloseOperation closed")
                }
            }

        })

        return call
    }
}