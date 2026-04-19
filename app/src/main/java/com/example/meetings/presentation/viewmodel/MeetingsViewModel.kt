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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadMeetings()
    }

    fun loadMeetings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repository.fetchMeetings()
                _meetings.postValue(data)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Неизвестная ошибка")
            } finally {
                _isLoading.value = false
            }
        }
    }
}