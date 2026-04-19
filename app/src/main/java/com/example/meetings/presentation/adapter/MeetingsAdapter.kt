package com.example.meetings.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.meetings.R
import com.example.meetings.data.model.Meeting
import com.example.meetings.databinding.ItemMeetingBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeetingsAdapter(
    private val onItemClick: (Meeting) -> Unit,
    private val onListSizeChanged: (Int) -> Unit
) : RecyclerView.Adapter<MeetingsAdapter.ViewHolder>() {

    private var meetings = listOf<Meeting>()

    fun submitList(newList: List<Meeting>) {
        meetings = newList
        notifyDataSetChanged()
        onListSizeChanged(meetings.size)
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
        private val dateFormat = SimpleDateFormat("dd.MM yyyy HH:mm:ss", Locale.getDefault())
        private val simpleTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        private val outputDateFormatter = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
        fun bind(meeting: Meeting, onClick: (Meeting) -> Unit) {
            binding.tvMeetingName.text = meeting.name

            try {
                val cleanDateString = meeting.createdAt.replace(Regex("\\.[0-9]+"), "")

                val parsedDate: Date? = simpleTimeFormat.parse(cleanDateString)
                if (parsedDate != null) {
                    binding.tvDate.text = outputDateFormatter.format(parsedDate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding.tvAuthor.text = meeting.author

            val minutes = meeting.duration / 60
            val seconds = meeting.duration % 60
            binding.tvDuration.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

            val statusIconResId = when (meeting.status.uppercase()) {
                "ARCHIVED" -> R.drawable.ic_status_archived
                "NEW"      -> R.drawable.ic_status_new
                "PROCESSED"-> R.drawable.ic_status_processed
                "END"      -> R.drawable.ic_status_end
                else       -> R.drawable.ic_status_archived
            }
            binding.ivMeetingStatus.setImageResource(statusIconResId)
            binding.root.setOnClickListener { onClick(meeting) }
        }
    }
}