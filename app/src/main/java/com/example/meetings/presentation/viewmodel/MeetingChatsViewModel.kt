package com.example.meetings.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetings.data.model.Chat
import com.example.meetings.data.repository.MeetingRepository
import kotlinx.coroutines.launch

class MeetingChatsViewModel(private val meetingId: String) : ViewModel() {

    private val repository = MeetingRepository()

    private val _chats = MutableLiveData<List<Chat>>()
    val chats: LiveData<List<Chat>> = _chats

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            try {
                val data = repository.getChatsByMeetingId(meetingId)
                _chats.postValue(data)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Не удалось загрузить чаты")
            }
        }
    }
}