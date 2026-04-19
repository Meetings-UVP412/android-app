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

        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnDetails -> {
                        binding.viewPager.currentItem = 0
                        updateIcons(false, true)
                    }
                    R.id.btnChat -> {
                        binding.viewPager.currentItem = 1
                        updateIcons(true, false)
                    }
                }
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.toggleGroup.check(R.id.btnDetails)
                        updateIcons(false, true)
                    }
                    1 -> {
                        binding.toggleGroup.check(R.id.btnChat)
                        updateIcons(true, false)
                    }
                }
            }
        })

        updateIcons(false, true)

    }

    private fun updateIcons(isChatActive: Boolean, isDetailsActive: Boolean) {
        binding.btnDetails.iconTint =
            ContextCompat.getColorStateList(requireContext(),
                if (isDetailsActive) R.color.main_dark else R.color.main_dark_blue)

        binding.btnChat.iconTint =
            ContextCompat.getColorStateList(requireContext(),
                if (isChatActive) R.color.main_dark else R.color.main_dark_blue)
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