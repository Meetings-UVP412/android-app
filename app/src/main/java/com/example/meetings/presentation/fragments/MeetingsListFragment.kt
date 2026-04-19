package com.example.meetings.presentation.fragments

import android.graphics.PorterDuff
import com.example.meetings.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
        adapter = MeetingsAdapter(
            onItemClick = { meeting ->
                findNavController().navigate(
                    R.id.action_to_meeting_detail,
                    Bundle().apply {
                        putString("meetingId", meeting.uuid)
                    }
                )
            },
            onListSizeChanged = { count ->
                val toolbarTitle = requireActivity().findViewById<TextView>(R.id.tv_meeting_history_title)
                toolbarTitle.text = "История встреч ($count)"
            }
        )
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
        val options = arrayOf(
            "Создать новую встречу",
            "Загрузить аудио встречи"
        )

        val icons = intArrayOf(
            R.drawable.ic_create_meeting,
            R.drawable.ic_upload_chunk
        )

        val iconTint = ContextCompat.getColor(requireContext(), R.color.main_white)

        val adapter = object : BaseAdapter() {
            override fun getCount() = options.size
            override fun getItem(position: Int) = options[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_alert_dialog, parent, false)

                val text = view.findViewById<TextView>(R.id.text)
                val icon = view.findViewById<ImageView>(R.id.icon)

                text.text = options[position]
                icon.setImageResource(icons[position])
                icon.setColorFilter(iconTint, PorterDuff.Mode.SRC_IN)

                return view
            }
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setAdapter(adapter) { _, which ->
                when (which) {
                    0 -> Toast.makeText(requireContext(), "Создание встречи!", Toast.LENGTH_LONG).show()
                    1 -> findNavController().navigate(R.id.screen_upload_audio)
                }
            }
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background_rounded)

            try {
                val dividerId = resources.getIdentifier("titleDivider", "id", "android")
                val divider = dialog.findViewById<View>(dividerId)
                divider?.visibility = View.GONE
            } catch (_: Exception) {
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}