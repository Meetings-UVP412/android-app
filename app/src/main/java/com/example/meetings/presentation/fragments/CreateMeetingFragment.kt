package com.example.meetings.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.meetings.R
import com.example.meetings.databinding.FragmentCreateMeetingBinding
import com.example.meetings.network.MeetingCreateRequest
import com.example.meetings.presentation.viewmodel.MeetingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CreateMeetingFragment : Fragment() {

    private var _binding: FragmentCreateMeetingBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MeetingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateMeetingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MeetingsViewModel::class.java]

        requireActivity().findViewById<TextView>(R.id.toolbar_title)?.text = "Создать встречу"

        binding.btnCreateMeeting.setOnClickListener {
            createMeeting()
        }

        binding.etMeetingName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etMeetingName.windowToken, 0)
                binding.etMeetingName.clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun createMeeting() {
        if (!binding.btnCreateMeeting.isEnabled) return

        val name = binding.etMeetingName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название встречи", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnCreateMeeting.isEnabled = false
        binding.btnCreateMeeting.alpha = 0.5f

        val request = MeetingCreateRequest(
            name = name,
            users = listOf(1, 2),
            authorId = 1,
            link = "",
            comment = ""
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val meeting = viewModel.createMeeting(request)

                delay(2000)

                findNavController().navigate(
                    CreateMeetingFragmentDirections.actionToRecording(
                        meetingId = meeting.uuid,
                        meetingName = meeting.name
                    )
                )
            } catch (e: Exception) {
                Log.e("CreateMeeting", "Error creating meeting", e)
                Toast.makeText(requireContext(), "Ошибка создания встречи", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnCreateMeeting.isEnabled = true
                binding.btnCreateMeeting.alpha = 1.0f
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}