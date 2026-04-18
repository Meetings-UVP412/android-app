package com.example.meetings.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetings.data.model.Chat
import com.example.meetings.data.repository.MeetingRepository
import kotlinx.coroutines.launch

class ChatDetailViewModel(private val chatId: String) : ViewModel() {

    private val repository = MeetingRepository()

    private val _chat = MutableLiveData<Chat>()
    val chat: LiveData<Chat> = _chat

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadChat()
    }

    private fun loadChat() {
        viewModelScope.launch {
            try {
                val data = repository.getChatHistory(chatId)
                _chat.postValue(data)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Не удалось загрузить историю чата")
            }
        }
    }
}