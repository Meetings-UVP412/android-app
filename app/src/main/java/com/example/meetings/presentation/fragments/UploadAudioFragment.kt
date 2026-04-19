package com.example.meetings.presentation.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.meetings.R
import com.example.meetings.databinding.FragmentUploadAudioBinding
import com.example.meetings.presentation.viewmodel.UploadAudioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UploadAudioFragment : Fragment() {
    private lateinit var binding: FragmentUploadAudioBinding

    private val viewModel: UploadAudioViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return UploadAudioViewModel(requireActivity().application) as T
            }
        }
    }

    private val meetingNameWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setMeetingName(s.toString())
        }
    }

    private val linkWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setLink(s.toString())
        }
    }

    private val commentWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setComment(s.toString())
        }
    }

    private var selectedDateTime: String = ""
    private val REQUEST_CODE_AUDIO_PICKER = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        setupTextWatchers()

        val toolbarTitle = requireActivity().findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.text = "Загрузить аудио встречи"
    }

    private fun setupObservers() {
        viewModel.meetingName.observe(viewLifecycleOwner) {
            binding.etMeetingName.removeTextChangedListener(meetingNameWatcher)
            binding.etMeetingName.setText(it)
            binding.etMeetingName.setSelection(binding.etMeetingName.text.length)
            binding.etMeetingName.addTextChangedListener(meetingNameWatcher)
        }

        viewModel.link.observe(viewLifecycleOwner) {
            binding.etLink.removeTextChangedListener(linkWatcher)
            binding.etLink.setText(it)
            binding.etLink.setSelection(binding.etLink.text.length)
            binding.etLink.addTextChangedListener(linkWatcher)
        }

        viewModel.comment.observe(viewLifecycleOwner) {
            binding.etComment.removeTextChangedListener(commentWatcher)
            binding.etComment.setText(it)
            binding.etComment.setSelection(binding.etComment.text.length)
            binding.etComment.addTextChangedListener(commentWatcher)
        }

        viewModel.isAudioFileSelected.observe(viewLifecycleOwner) { isSelected ->
            binding.ivWaveform.visibility = if (isSelected) View.VISIBLE else View.GONE
            binding.wrapWaverForm.visibility = if (isSelected) View.VISIBLE else View.GONE
            binding.btnDeleteAudio.visibility = if (isSelected) View.VISIBLE else View.GONE
            binding.btnUploadAudio.visibility = if (isSelected) View.GONE else View.VISIBLE
        }

        viewModel.meetingTime.observe(viewLifecycleOwner) { time ->
            binding.tvDateTime.text = time
        }

        viewModel.isCreateButtonEnabled.observe(viewLifecycleOwner) {
            binding.btnCreateMeeting.isEnabled = it
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        viewModel.success.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.screen_meetings_list)
        }
    }

    private fun setupClickListeners() {
        binding.tvDateTime.setOnClickListener { showDateTimePicker() }
        binding.btnUploadAudio.setOnClickListener { openFilePicker() }
        binding.btnDeleteAudio.setOnClickListener { viewModel.clearAudioFile() }

        binding.btnCreateMeeting.setOnClickListener {
            if (!viewModel.isCreateButtonEnabled.value!!) {
                Toast.makeText(requireContext(), "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.createMeeting()
            }
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val dateDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                val timeDialog = TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        selectedDateTime = formatDate(calendar.time)
                        viewModel.setMeetingTime(selectedDateTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                timeDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dateDialog.show()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_CODE_AUDIO_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_AUDIO_PICKER && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                Log.d("UploadAudio", "Selected URI: $uri")
                viewModel.setAudioFile(uri)
                calculateAudioDuration(uri)
            }
        }
    }

    private fun calculateAudioDuration(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            var duration = 0
            val retriever = MediaMetadataRetriever()
            try {
                try {
                    retriever.setDataSource(requireContext(), uri)
                    val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
                    duration = (durationMs / 1000).toInt()
                } catch (e1: Exception) {
                    Log.w("AudioDuration", "Первый способ не сработал", e1)
                    requireContext().contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
                        retriever.setDataSource(parcelFileDescriptor.fileDescriptor)
                        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
                        duration = (durationMs / 1000).toInt()
                    }
                }
                viewModel.setAudioDuration(duration)

                withContext(Dispatchers.Main) {
                    binding.ivWaveform.visibility = View.VISIBLE
                    binding.wrapWaverForm.visibility = View.VISIBLE
                    binding.btnDeleteAudio.visibility = View.VISIBLE
                    binding.btnUploadAudio.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("AudioDuration", "Обе попытки не удалась", e)
                viewModel.setAudioDuration(0)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось определить длительность аудио. Используем 0 сек.",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.ivWaveform.visibility = View.VISIBLE
                    binding.wrapWaverForm.visibility = View.VISIBLE
                    binding.btnDeleteAudio.visibility = View.VISIBLE
                    binding.btnUploadAudio.visibility = View.GONE
                }
            } catch (e1: Exception) {
                Log.w("AudioDuration", "Ошибка MediaMetadataRetriever: ${e1.message}", e1)
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    Log.w("AudioDuration", "Ошибка при освобождении retriever", e)
                }
            }
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd MMM. yyyy г. HH:mm", Locale.getDefault()).format(date)
    }

    private fun setupTextWatchers() {
        binding.etMeetingName.addTextChangedListener(meetingNameWatcher)
        binding.etLink.addTextChangedListener(linkWatcher)
        binding.etComment.addTextChangedListener(commentWatcher)
    }
}