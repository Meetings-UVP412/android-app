package com.example.meetings.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.meetings.data.model.Message
import com.example.meetings.databinding.ItemAssistantMessageBinding
import com.example.meetings.databinding.ItemUserMessageBinding

class ChatMessagesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = emptyList<Message>()

    fun submitList(newList: List<Message>) {
        messages = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> UserMessageViewHolder(
                ItemUserMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> AssistantMessageViewHolder(
                ItemAssistantMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("ChatMessagesAdapter", "Binding message: ${messages[position].content}")
        when (holder) {
            is UserMessageViewHolder -> holder.bind(messages[position])
            is AssistantMessageViewHolder -> holder.bind(messages[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].role == "user") 0 else 1
    }

    override fun getItemCount(): Int {
        Log.d("ChatMessagesAdapter", "getItemCount: ${messages.size}")
        return messages.size
    }

    class UserMessageViewHolder(private val binding: ItemUserMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.content
        }
    }

    class AssistantMessageViewHolder(private val binding: ItemAssistantMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.content
        }
    }
}