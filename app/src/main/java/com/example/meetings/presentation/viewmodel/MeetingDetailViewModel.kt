package com.example.meetings.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetings.data.model.Meeting
import com.example.meetings.data.repository.MeetingRepository
import kotlinx.coroutines.launch

class MeetingDetailViewModel(private val meetingId: String) : ViewModel() {

    private val repository = MeetingRepository()

    private val _meeting = MutableLiveData<Meeting>()
    val meeting: LiveData<Meeting> = _meeting

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadMeeting()
    }

    private fun loadMeeting() {
        viewModelScope.launch {
            try {
                val data = repository.getMeetingById(meetingId)
                _meeting.postValue(data)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Не удалось загрузить данные встречи")
            }
        }
    }
}