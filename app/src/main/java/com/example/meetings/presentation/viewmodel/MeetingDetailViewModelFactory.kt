package com.example.meetings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MeetingDetailViewModelFactory(private val meetingId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeetingDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeetingDetailViewModel(meetingId) as T
        }
        throw IllegalArgumentException("Неизвестный класс ViewModel")
    }
}