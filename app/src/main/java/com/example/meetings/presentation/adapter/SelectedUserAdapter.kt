package com.example.meetings.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.meetings.data.model.User
import com.example.meetings.databinding.ItemMeetingParticipantBinding

class SelectedUserAdapter : ListAdapter<User, SelectedUserAdapter.ViewHolder>(UserDiffCallback()) {

    inner class ViewHolder(private val binding: ItemMeetingParticipantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvUserName.text = "${user.firstName}\n${user.patronymic}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMeetingParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}