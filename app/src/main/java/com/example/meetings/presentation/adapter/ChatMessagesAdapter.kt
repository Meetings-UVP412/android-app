package com.example.meetings.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.meetings.data.model.Message
import com.example.meetings.databinding.ItemAssistantMessageBinding
import com.example.meetings.databinding.ItemUserMessageBinding

class ChatMessagesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = emptyList<Message>()

    fun submitList(newList: List<Message>) {
        Log.d("Adapter", "submitList called with ${newList.size} messages")
        val diffCallback = MessageDiffCallback(messages, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messages = newList
        diffResult.dispatchUpdatesTo(this)
        Log.d("Adapter", "Adapter notified of changes")
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
        val message = messages[position]
        Log.d("Adapter", "BindingUtil position $position: ${message.role} - [${message.content}]")
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AssistantMessageViewHolder -> holder.bind(message)
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

    class MessageDiffCallback(
        private val oldList: List<Message>,
        private val newList: List<Message>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].role == newList[newItemPosition].role
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}