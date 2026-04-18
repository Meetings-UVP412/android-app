package com.example.meetings.presentation.viewmodel

import android.util.Log
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

    private val assistantResponseBuffer = StringBuilder()

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

    fun sendMessage(messageText: String) {
        val userMessage = Message(role = "user", content = messageText)
        val request = SendMessageRequest(
            meetingUUID = meetingId,
            chatUUID = chatId,
            message = userMessage
        )

        _isStreaming.postValue(true)

        val currentChat = _chat.value?.copy() ?: return
        val updatedMessages = currentChat.messages + userMessage
        _chat.postValue(currentChat.copy(messages = updatedMessages))

        assistantResponseBuffer.clear()

        currentStreamingCall = repository.sendMessageAndStream(
            request,
            onChunkReceived = { chunk ->
                Log.d("ViewModel", "Chunk received: [$chunk]")
                assistantResponseBuffer.append(chunk)

                /*
                val current = _chat.value ?: return@onChunkReceived
                val messages = current.messages.toMutableList()
                if (messages.lastOrNull()?.role == "assistant") {
                    messages[messages.lastIndex] = messages.last().copy(content = assistantResponseBuffer.toString())
                } else {
                    messages.add(Message(role = "assistant", content = assistantResponseBuffer.toString()))
                }
                _chat.postValue(current.copy(messages = messages))
                */
            },
            onError = { error ->
                _isStreaming.postValue(false)
                _error.postValue(error.message ?: "Ошибка при получении ответа")
            },
            onComplete = onComplete@{
                _isStreaming.postValue(false)

                val finalContent = assistantResponseBuffer.toString().trim()
                if (finalContent.isNotEmpty()) {
                    val current = _chat.value ?: return@onComplete
                    val messages = current.messages.toMutableList()
                    messages.add(Message(role = "assistant", content = finalContent))
                    _chat.postValue(current.copy(messages = messages))
                }
            }
        )
    }

    override fun onCleared() {
        currentStreamingCall?.cancel()
        super.onCleared()
    }
}