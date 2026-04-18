package com.example.meetings.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.meetings.R
import com.example.meetings.databinding.FragmentMeetingDetailBinding
import com.example.meetings.presentation.adapter.MeetingDetailPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MeetingDetailFragment : Fragment() {

    private var _binding: FragmentMeetingDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeetingDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbarTitle = requireActivity().findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.text = "Карточка встречи"

        val meetingId = arguments?.getString("meetingId") ?: ""

        val adapter = MeetingDetailPagerAdapter(meetingId, this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Детали встречи"
                1 -> "Чаты"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeAsUpIndicator(null)
        }
        _binding = null
    }
}