package com.example.meetings.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.meetings.data.model.User
import com.example.meetings.databinding.ItemUserBinding

class AllUserAdapter(
    private val onUserSelected: (User, Boolean) -> Unit
) : ListAdapter<User, AllUserAdapter.ViewHolder>(UserDiffCallback()) {

    private val selectedUserIds = mutableSetOf<Int>()

    fun updateSelectedIds(ids: Set<Int>) {
        if (selectedUserIds != ids) {
            val changedPositions = mutableListOf<Int>()
            currentList.forEachIndexed { index, user ->
                val wasSelected = selectedUserIds.contains(user.id)
                val isSelected = ids.contains(user.id)
                if (wasSelected != isSelected) {
                    changedPositions.add(index)
                }
            }

            selectedUserIds.clear()
            selectedUserIds.addAll(ids)

            changedPositions.forEach { notifyItemChanged(it) }
        }
    }

    inner class ViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, isSelected: Boolean) {
            binding.tvUserName.text = "${user.lastName} ${user.firstName} ${user.patronymic}"

            binding.cbSelect.setOnCheckedChangeListener(null)
            binding.cbSelect.isChecked = isSelected
            binding.cbSelect.setOnCheckedChangeListener { _, isChecked ->
                onUserSelected(user, isChecked)
            }

            binding.root.setOnClickListener {
                binding.cbSelect.toggle()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, selectedUserIds.contains(user.id))
    }
}