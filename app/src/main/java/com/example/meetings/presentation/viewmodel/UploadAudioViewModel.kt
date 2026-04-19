package com.example.meetings.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.meetings.data.repository.MeetingRepository
import com.example.meetings.network.MeetingCreateRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UploadAudioViewModel(application: Application) : AndroidViewModel(application) {
    private val _meetingName = MutableLiveData<String>()
    val meetingName: LiveData<String> = _meetingName

    private val _meetingTime = MutableLiveData<String>(getCurrentDateTime())
    val meetingTime: LiveData<String> = _meetingTime

    companion object {
        fun getCurrentDateTime(): String {
            return SimpleDateFormat("dd MMM. yyyy г. HH:mm", Locale.getDefault()).format(Date())
        }
    }

    private val _link = MutableLiveData<String>()
    val link: LiveData<String> = _link

    private val _comment = MutableLiveData<String>()
    val comment: LiveData<String> = _comment

    private val _audioFile = MutableLiveData<Uri?>()
    val audioFile: LiveData<Uri?> = _audioFile

    private val _audioDuration = MutableLiveData<Int>()
    val audioDuration: LiveData<Int> = _audioDuration

    private val _isAudioFileSelected = MutableLiveData<Boolean>()
    val isAudioFileSelected: LiveData<Boolean> = _isAudioFileSelected

    private val _isCreateButtonEnabled = MutableLiveData<Boolean>()
    val isCreateButtonEnabled: LiveData<Boolean> = _isCreateButtonEnabled

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    private val repository = MeetingRepository()

    init {
    }

    fun setMeetingName(name: String) {
        _meetingName.value = name
        updateButtonState()
    }

    fun setMeetingTime(time: String) {
        _meetingTime.value = time
        updateButtonState()
    }

    fun setLink(link: String) {
        _link.value = link
        updateButtonState()
    }

    fun setComment(comment: String) {
        _comment.value = comment
        updateButtonState()
    }

    fun setAudioFile(file: Uri) {
        _audioFile.value = file
        _isAudioFileSelected.value = true
        updateButtonState()
    }

    fun clearAudioFile() {
        _audioFile.value = null
        _audioDuration.value = 0
        _isAudioFileSelected.value = false
        updateButtonState()
    }

    private fun updateButtonState() {
        val isNameValid = _meetingName.value?.isNotEmpty() == true
        val isAudioValid = _audioFile.value != null
        val isTimeValid = true
        val enabled = isNameValid && isAudioValid && isTimeValid
        Log.d("ButtonState", "Name:$isNameValid Audio:$isAudioValid Time:$isTimeValid -> Enabled:$enabled")

        if (_isCreateButtonEnabled.value != enabled) {
            _isCreateButtonEnabled.value = enabled
        }
    }

    fun createMeeting() {
        if (!_isCreateButtonEnabled.value!!) {
            _error.value = "Заполните все обязательные поля"
            return
        }

        viewModelScope.launch {
            try {
                val meeting = repository.createMeeting(
                    MeetingCreateRequest(
                        name = _meetingName.value!!,
                        users = emptyList(),
                        authorId = 21,
                        link = _link.value,
                        comment = _comment.value
                    )
                )

                repository.uploadAudioFile(
                    context = getApplication(),
                    meetingId = meeting.uuid,
                    order = 1,
                    isLast = true,
                    duration = _audioDuration.value ?: 0,
                    file = _audioFile.value!!
                )

                _success.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка при создании встречи"
            }
        }
    }

    fun setAudioDuration(duration: Int) {
        _audioDuration.postValue(duration)
    }
}