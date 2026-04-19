package com.example.meetings.presentation.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.meetings.R
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

            binding.tvMessage.setOnLongClickListener {
                showCopyDialog(it.context, message.content)
                true
            }
        }

        private fun showCopyDialog(context: Context, text: String) {
            val dialog = AlertDialog.Builder(context, R.style.RoundedAlertDialogCopy)
                .setItems(arrayOf("Копировать")) { _, _ ->
                    copyToClipboard(context, text)
                }
                .create()

            dialog.setOnShowListener {
                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background_rounded)
            }

            dialog.show()
        }

        private fun copyToClipboard(context: Context, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Сообщение", text))
            Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
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