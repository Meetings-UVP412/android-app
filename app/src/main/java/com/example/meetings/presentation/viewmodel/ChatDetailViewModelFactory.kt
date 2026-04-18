package com.example.meetings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatDetailViewModelFactory(
    private val chatId: String,
    private val meetingId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatDetailViewModel(chatId, meetingId) as T
        }
        throw IllegalArgumentException("Неизвестный класс ViewModel")
    }
}