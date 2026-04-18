package com.example.meetings.data.repository

import com.example.meetings.data.model.Chat
import com.example.meetings.data.model.Meeting
import com.example.meetings.network.MeetingApi

class MeetingRepository {
    suspend fun fetchMeetings(): List<Meeting> {
        return MeetingApi.service.getMeetings()
    }

    suspend fun getMeetingById(meetingId: String): Meeting {
        return MeetingApi.service.getMeetingById(meetingId)
    }

    suspend fun getChatsByMeetingId(meetingId: String): List<Chat> {
        return MeetingApi.service.getChatsByMeetingId(meetingId)
    }
}