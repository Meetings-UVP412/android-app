package com.example.meetings.presentation.fragments

import com.example.meetings.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meetings.databinding.FragmentMeetingsListBinding
import com.example.meetings.presentation.adapter.MeetingsAdapter
import com.example.meetings.presentation.viewmodel.MeetingsViewModel
import androidx.navigation.fragment.findNavController

class MeetingsListFragment : Fragment() {

    private var _binding: FragmentMeetingsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MeetingsAdapter
    private lateinit var viewModel: MeetingsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMeetingsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbarTitle = requireActivity().findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.text = "Встречи"

        setupRecyclerView()
        setupViewModel()
        setupClicks()
    }

    private fun setupRecyclerView() {
        adapter = MeetingsAdapter { meeting ->
            findNavController().navigate(
                R.id.action_to_meeting_detail,
                Bundle().apply {
                    putString("meetingId", meeting.uuid)
                }
            )
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MeetingsViewModel::class.java]
        viewModel.meetings.observe(viewLifecycleOwner) { meetings ->
            adapter.submitList(meetings)
        }
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClicks() {
        binding.btnCreateMeeting.setOnClickListener {
            showCreateOptions()
        }
    }

    private fun showCreateOptions() {
        // TODO: Показать BottomSheetDialog или отдельный фрагмент с двумя кнопками
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}