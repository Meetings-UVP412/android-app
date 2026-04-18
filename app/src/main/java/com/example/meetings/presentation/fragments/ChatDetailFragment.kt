package com.example.meetings.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meetings.R
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

        binding.etMessageInput.setOnClickListener {
            requestFocusAndShowKeyboard()
        }

        binding.etMessageInput.setOnTouchListener { _, _ ->
            requestFocusAndShowKeyboard()
            false
        }
    }

    private fun setupArguments() {
        val chatId = arguments?.getString("chatId") ?: ""
        val meetingId = arguments?.getString("meetingId") ?: ""
        viewModel = ViewModelProvider(
            this,
            ChatDetailViewModelFactory(chatId, meetingId)
        )[ChatDetailViewModel::class.java]
    }

    private fun setupRecyclerView() {
        messagesAdapter = ChatMessagesAdapter()
        binding.rvMessages.adapter = messagesAdapter
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupViewModel() {
        viewModel.isStreaming.observe(viewLifecycleOwner) { isStreaming ->
            binding.ivSendButton.isEnabled = !isStreaming
            if (isStreaming) {
                binding.ivSendButton.setImageResource(R.drawable.ic_button_sending)
            } else {
                binding.ivSendButton.setImageResource(R.drawable.ic_send_button)
            }
        }

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
        binding.etMessageInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.rvMessages.postDelayed({
                    binding.rvMessages.smoothScrollToPosition(messagesAdapter.itemCount - 1)
                }, 200)
            }
        }

        binding.etMessageInput.setOnClickListener {
            showKeyboard()
        }

        binding.ivSendButton.setOnClickListener {
            val messageText = binding.etMessageInput.text.toString().trim()
            if (messageText.isNotEmpty() && !viewModel.isStreaming.value!!) {
                Log.d("Fragment", "📤 Sending user message: [$messageText]")
                viewModel.sendMessage(messageText)
                binding.etMessageInput.setText("")
            }
        }


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

    private fun showKeyboard() {
        binding.etMessageInput.requestFocus()
        binding.etMessageInput.postDelayed({
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etMessageInput, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun requestFocusAndShowKeyboard() {
        binding.etMessageInput.requestFocus()
        binding.etMessageInput.postDelayed({
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etMessageInput, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}