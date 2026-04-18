package com.example.meetings.presentation.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.meetings.presentation.fragments.ChatsListFragment
import com.example.meetings.presentation.fragments.MeetingDetailsFragment

class MeetingDetailPagerAdapter(private val meetingId: String, fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MeetingDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString("meetingId", meetingId)
                }
            }
            1 -> ChatsListFragment().apply {
                arguments = Bundle().apply {
                    putString("meetingId", meetingId)
                }
            }
            else -> MeetingDetailsFragment()
        }
    }
}