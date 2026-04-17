package com.example.meetings.network

import com.example.meetings.data.Meeting
import retrofit2.http.GET

interface ApiService {
    @GET("api/meetings")
    suspend fun getMeetings(): List<Meeting>
}