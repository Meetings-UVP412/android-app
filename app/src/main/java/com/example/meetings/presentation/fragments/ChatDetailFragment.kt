package com.example.meetings.presentation.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meetings.databinding.FragmentChatDetailBinding
import com.example.meetings.presentation.adapter.ChatMessagesAdapter
import com.example.meetings.presentation.viewmodel.ChatDetailViewModel
import com.example.meetings.presentation.viewmodel.ChatDetailViewModelFactory

class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatDetailViewModel
    private lateinit var messagesAdapter: ChatMessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupArguments()
        setupRecyclerView()
        setupViewModel()
        setupInputField()
    }

    private fun setupArguments() {
        val chatId = arguments?.getString("chatId") ?: ""
        viewModel = ViewModelProvider(this, ChatDetailViewModelFactory(chatId)) [ChatDetailViewModel::class.java]
    }

    private fun setupRecyclerView() {
        messagesAdapter = ChatMessagesAdapter()
        binding.rvMessages.adapter = messagesAdapter
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupViewModel() {
        viewModel.chat.observe(viewLifecycleOwner) { chat ->
            messagesAdapter.submitList(chat.messages)

            binding.rvMessages.postDelayed({
                binding.rvMessages.smoothScrollToPosition(messagesAdapter.itemCount - 1)
            }, 300)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupInputField() {
        binding.etMessageInput.setOnTouchListener { _, _ ->
            binding.etMessageInput.isFocusable = true
            binding.etMessageInput.isFocusableInTouchMode = true
            true
        }

        binding.etMessageInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adjustInputHeight()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun adjustInputHeight() {
        val lineHeight = binding.etMessageInput.lineHeight
        val lines = binding.etMessageInput.lineCount
        val padding = binding.etMessageInput.paddingTop + binding.etMessageInput.paddingBottom

        val newHeight = (lineHeight * lines) + padding
        val maxHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 150f, requireContext().resources.displayMetrics
        ).toInt()

        binding.etMessageInput.layoutParams.height = minOf(newHeight, maxHeight)
        binding.etMessageInput.requestLayout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}