package com.example.meetings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MeetingChatsViewModelFactory(private val meetingId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeetingChatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeetingChatsViewModel(meetingId) as T
        }
        throw IllegalArgumentException("Неизвестный класс ViewModel")
    }
}