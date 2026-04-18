package com.example.meetings.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meetings.databinding.FragmentChatsListBinding
import com.example.meetings.presentation.adapter.ChatsAdapter
import com.example.meetings.presentation.viewmodel.MeetingChatsViewModel
import com.example.meetings.presentation.viewmodel.MeetingChatsViewModelFactory

class ChatsListFragment : Fragment() {

    private var _binding: FragmentChatsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MeetingChatsViewModel
    private lateinit var chatsAdapter: ChatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsListBinding.inflate(inflater, container, false)
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
        viewModel = ViewModelProvider(this, MeetingChatsViewModelFactory(meetingId)) [MeetingChatsViewModel::class.java]
    }

    private fun setupRecyclerView() {
        chatsAdapter = ChatsAdapter()
        binding.rvChats.adapter = chatsAdapter
        binding.rvChats.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupViewModel() {
        viewModel.chats.observe(viewLifecycleOwner) { chats ->
            chatsAdapter.submitList(chats)
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