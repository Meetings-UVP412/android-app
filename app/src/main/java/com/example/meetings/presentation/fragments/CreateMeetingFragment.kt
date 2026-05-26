package com.example.meetings.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.meetings.R
import com.example.meetings.data.model.User
import com.example.meetings.databinding.FragmentCreateMeetingBinding
import com.example.meetings.network.MeetingCreateRequest
import com.example.meetings.presentation.adapter.AllUserAdapter
import com.example.meetings.presentation.adapter.SelectedUserAdapter
import com.example.meetings.presentation.viewmodel.MeetingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CreateMeetingFragment : Fragment() {

    private var _binding: FragmentCreateMeetingBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MeetingsViewModel

    private val selectedUsers = mutableListOf<User>()
    private var allUsers: List<User> = emptyList()

    private lateinit var allUserAdapter: AllUserAdapter
    private lateinit var selectedUserAdapter: SelectedUserAdapter

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

        setupAdapters()
        setupListeners()
        loadUsers()
    }

    private fun setupAdapters() {
        selectedUserAdapter = SelectedUserAdapter()
        binding.rvSelectedUsers.adapter = selectedUserAdapter

        allUserAdapter = AllUserAdapter { user, isChecked ->
            if (isChecked) {
                if (!selectedUsers.contains(user)) {
                    selectedUsers.add(user)
                }
            } else {
                selectedUsers.remove(user)
            }
            selectedUserAdapter.submitList(selectedUsers.toList())
            updateParticipantsCount()
            allUserAdapter.updateSelectedIds(selectedUsers.map { it.id }.toSet())
        }
        binding.rvAllUsers.adapter = allUserAdapter
    }

    private fun setupListeners() {
        binding.btnCreateMeeting.setOnClickListener {
            createMeeting()
        }

        binding.etMeetingName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                binding.etMeetingName.clearFocus()
                true
            } else {
                false
            }
        }

        binding.etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterUsers(s.toString())
            }
        })
    }

    private fun loadUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                allUsers = viewModel.getUsers()
                allUserAdapter.submitList(allUsers)
            } catch (e: Exception) {
                Log.e("CreateMeeting", "Error loading users", e)
                Toast.makeText(requireContext(), "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterUsers(query: String) {
        val searchQuery = query.lowercase().trim()
        if (searchQuery.isEmpty()) {
            allUserAdapter.submitList(allUsers)
        } else {
            val filtered = allUsers.filter { user ->
                user.firstName.lowercase().contains(searchQuery) ||
                        user.lastName.lowercase().contains(searchQuery) ||
                        user.patronymic.lowercase().contains(searchQuery)
            }
            allUserAdapter.submitList(filtered)
        }
    }

    private fun updateParticipantsCount() {
        binding.tvSelectedUsersTitle.text = "Участники (${selectedUsers.size})"
    }

    private fun createMeeting() {
        if (!binding.btnCreateMeeting.isEnabled) return

        val name = binding.etMeetingName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название встречи", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedUsers.isEmpty()) {
            Toast.makeText(requireContext(), "Выберите хотя бы одного участника", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnCreateMeeting.isEnabled = false
        binding.btnCreateMeeting.alpha = 0.5f

        val request = MeetingCreateRequest(
            name = name,
            users = selectedUsers.map { it.id },
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
                        meetingName = meeting.name,
                        participantsCount = meeting.users.size,
                        meetingDate = meeting.createdAt
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

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etMeetingName.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}