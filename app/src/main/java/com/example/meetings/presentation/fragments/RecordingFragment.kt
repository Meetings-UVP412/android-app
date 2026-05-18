package com.example.meetings.presentation.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.meetings.R
import com.example.meetings.databinding.FragmentRecordingBinding
import com.example.meetings.presentation.service.RecordingService

class RecordingFragment : Fragment() {

    private var _binding: FragmentRecordingBinding? = null
    private val binding get() = _binding!!
    private var isRecording = false
    private var elapsedTime = 0L
    private var timer: CountDownTimer? = null
    private lateinit var meetingId: String
    private var chunkOrder = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        meetingId = requireArguments().getString("meetingId") ?: ""
        val meetingName = requireArguments().getString("meetingName") ?: "Встреча"

        requireActivity().findViewById<TextView>(R.id.toolbar_title)?.text = meetingName

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnRecord.setOnClickListener {
            if (!isRecording) {
                startRecording()
            } else {
                stopRecording()
            }
        }

        binding.btnFinishMeeting.setOnClickListener {
            finishMeeting()
        }
    }

    private fun startRecording() {
        if (meetingId.isEmpty()) {
            Log.e("RecordingFragment", "meetingId is empty!")
            Toast.makeText(requireContext(), "Ошибка: ID встречи не задан", Toast.LENGTH_LONG).show()
            return
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(
                    requireContext(),
                    "Разрешение на запись необходимо для работы приложения",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
            }
            return
        }

        isRecording = true
        binding.btnRecord.setImageResource(R.drawable.ic_stop)
        binding.waveform.visibility = View.VISIBLE

        val intent = Intent(requireContext(), RecordingService::class.java).apply {
            putExtra("meetingId", meetingId)
        }
        requireContext().startService(intent)

        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime++
                updateTimer()
            }

            override fun onFinish() {}
        }.start()
    }

    private fun stopRecording() {
        isRecording = false
        binding.btnRecord.setImageResource(R.drawable.ic_record)
        binding.waveform.visibility = View.GONE
        timer?.cancel()
        timer = null
    }

    private fun finishMeeting() {
        val intent = Intent(requireContext(), RecordingService::class.java).apply {
            putExtra("isLast", true)
            putExtra("meetingId", meetingId)
            putExtra("lastChunkOrder", chunkOrder)
        }
        requireContext().startService(intent)

        val result = Bundle().apply {
            putBoolean("meeting_finished", true)
        }
        parentFragmentManager.setFragmentResult("meeting_request_key", result)
        findNavController().popBackStack(R.id.screen_meetings_list, false)
    }

    private fun updateTimer() {
        val hours = elapsedTime / 3600
        val minutes = (elapsedTime % 3600) / 60
        val seconds = elapsedTime % 60
        binding.tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        timer?.cancel()
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(
                        requireContext(),
                        "Разрешение на микрофон необходимо для записи встречи",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Требуется разрешение")
                        .setMessage("Для записи встречи нужно разрешение на использование микрофона. Перейдите в настройки приложения и включите разрешение.")
                        .setPositiveButton("Настройки") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", requireContext().packageName, null)
                            }
                            startActivity(intent)
                        }
                        .setNegativeButton("Отмена", null)
                        .show()
                }
            }
        }
    }
}