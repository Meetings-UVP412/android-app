package com.example.meetings.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.meetings.R
import com.example.meetings.databinding.FragmentMeetingDetailBinding
import com.example.meetings.presentation.adapter.MeetingDetailPagerAdapter

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

        binding.btnDetails.setOnClickListener { switchTab(0) }
        binding.btnChat.setOnClickListener { switchTab(1) }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                switchTab(position)
            }
        })

        switchTab(0)
    }

    private fun switchTab(position: Int) {
        binding.viewPager.currentItem = position
        if (position == 0) {
            binding.btnDetails.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_left_active)
            binding.btnDetails.setIconTintResource(R.color.main_white)
            binding.btnChat.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_right_inactive)
            binding.btnChat.setIconTintResource(R.color.tab_grey)

        } else {
            binding.btnChat.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_right_active)
            binding.btnChat.setIconTintResource(R.color.main_white)
            binding.btnDetails.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_left_inactive)
            binding.btnDetails.setIconTintResource(R.color.tab_grey)
        }
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