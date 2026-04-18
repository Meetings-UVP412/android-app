package com.example.meetings.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.meetings.R
import com.example.meetings.data.model.Chat
import com.example.meetings.databinding.ItemChatBinding

class ChatsAdapter(
    private val onItemClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    private var chats = emptyList<Chat>()

    fun submitList(newList: List<Chat>) {
        chats = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chats[position], onItemClick)
    }

    override fun getItemCount() = chats.size

    class ViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat, onItemClick: (Chat) -> Unit) {
            binding.tvChatTitle.text = chat.title

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val createdAt = try {
                val date = inputFormat.parse(chat.createdAt)
                if (date != null) outputFormat.format(date) else chat.createdAt
            } catch (e: Exception) {
                chat.createdAt
            }
            binding.tvChatTime.text = createdAt

            binding.ivChatIcon.setImageResource(R.drawable.ic_default_chat)

            binding.root.setOnClickListener {
                onItemClick(chat)
            }
        }
    }
}