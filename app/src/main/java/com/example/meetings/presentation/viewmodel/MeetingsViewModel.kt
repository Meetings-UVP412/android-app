package com.example.meetings.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetings.data.model.Meeting
import com.example.meetings.data.repository.MeetingRepository
import kotlinx.coroutines.launch

class MeetingsViewModel : ViewModel() {

    private val repository = MeetingRepository()

    private val _meetings = MutableLiveData<List<Meeting>>()
    val meetings: LiveData<List<Meeting>> = _meetings

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadMeetings()
    }

    private fun loadMeetings() {
        viewModelScope.launch {
            try {
                val data = repository.fetchMeetings()
                _meetings.postValue(data)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}