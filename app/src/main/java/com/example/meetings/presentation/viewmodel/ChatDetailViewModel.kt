package com.example.meetings.presentation.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetings.data.model.Chat
import com.example.meetings.data.model.Message
import com.example.meetings.data.model.SendMessageRequest
import com.example.meetings.data.repository.MeetingRepository
import kotlinx.coroutines.launch
import okhttp3.Call

class ChatDetailViewModel(
    private val chatId: String,
    private val meetingId: String
) : ViewModel() {

    private val repository = MeetingRepository()

    private val _chat = MutableLiveData<Chat>()
    val chat: LiveData<Chat> = _chat

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isStreaming = MutableLiveData<Boolean>().apply { value = false }
    val isStreaming: LiveData<Boolean> = _isStreaming

    private var currentStreamingCall: Call? = null

    private var currentAssistantMessage: Message? = null

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

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun sendMessage(messageText: String) {
        val userMessage = Message(role = "user", content = messageText)
        val request = SendMessageRequest(
            meetingUUID = meetingId,
            chatUUID = chatId,
            message = userMessage
        )

        _isStreaming.value = true

        val currentChat = _chat.value ?: return

        val messagesWithUser = currentChat.messages + userMessage

        val assistantMessage = Message(role = "assistant", content = "")
        val messagesWithAssistant = messagesWithUser + assistantMessage

        _chat.value = currentChat.copy(messages = messagesWithAssistant)

        currentStreamingCall = repository.sendMessageAndStream(
            request,
            onChunkReceived = { chunk ->
                Log.d("ViewModel", "Chunk received: [$chunk]")

                val currentMessages = _chat.value?.messages.orEmpty().toMutableList()
                if (currentMessages.lastOrNull()?.role == "assistant") {
                    val lastMessage = currentMessages.removeLast()
                    val updatedMessage = lastMessage.copy(content = lastMessage.content + chunk)
                    currentMessages.add(updatedMessage)

                    _chat.value = _chat.value?.copy(messages = currentMessages)
                }
            },
            onError = { error ->
                _isStreaming.value = false
                _error.value = error.message ?: "Ошибка при получении ответа"
            },
            onComplete = {
                _isStreaming.value = false
            }
        )
    }

    override fun onCleared() {
        currentStreamingCall?.cancel()
        super.onCleared()
    }
}