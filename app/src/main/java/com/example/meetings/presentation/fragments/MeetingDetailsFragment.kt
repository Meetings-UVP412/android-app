package com.example.meetings.presentation.fragments

import android.os.Bundle
import com.example.meetings.presentation.viewmodel.MeetingDetailViewModelFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.meetings.databinding.FragmentMeetingDetailsBinding
import com.example.meetings.presentation.adapter.ParticipantsAdapter
import com.example.meetings.presentation.viewmodel.MeetingDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class MeetingDetailsFragment : Fragment() {

    private var _binding: FragmentMeetingDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MeetingDetailViewModel
    private lateinit var participantsAdapter: ParticipantsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeetingDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupArguments()
        setupRecyclerView()
        setupViewModel()
    }

    private fun setupArguments() {
        val meetingId = arguments?.getString("meetingId") ?: ""
        viewModel = ViewModelProvider(this, MeetingDetailViewModelFactory(meetingId)) [MeetingDetailViewModel::class.java]
    }

    private fun setupRecyclerView() {
        participantsAdapter = ParticipantsAdapter()
        binding.rvParticipants.adapter = participantsAdapter
    }

    private fun setupViewModel() {
        viewModel.meeting.observe(viewLifecycleOwner) { meeting ->
            binding.tvMeetingName.text = meeting.name
            binding.tvLink.text = meeting.link
            binding.tvComment.text = meeting.comment ?: "Комментарий отсутствует"

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val startDate = try {
                val date = inputFormat.parse(meeting.createdAt)
                if (date != null) outputFormat.format(date) else meeting.createdAt
            } catch (e: Exception) {
                meeting.createdAt
            }
            binding.tvStartTime.text = startDate

            binding.tvEndTime.text = "13:16"

            binding.tvAuthor.text = meeting.author

            participantsAdapter.submitList(meeting.users)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}