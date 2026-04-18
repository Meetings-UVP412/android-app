package com.example.meetings.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.meetings.data.model.Meeting
import com.example.meetings.databinding.ItemMeetingBinding

class MeetingsAdapter(
    private val onItemClick: (Meeting) -> Unit) : RecyclerView.Adapter<MeetingsAdapter.ViewHolder>() {

    private var meetings = listOf<Meeting>()

    fun submitList(newList: List<Meeting>) {
        meetings = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMeetingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(meetings[position], onItemClick)
    }

    override fun getItemCount() = meetings.size

    class ViewHolder(private val binding: ItemMeetingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(meeting: Meeting, onClick: (Meeting) -> Unit) {
            binding.tvMeetingName.text = meeting.name
            binding.root.setOnClickListener { onClick(meeting) }
        }
    }
}