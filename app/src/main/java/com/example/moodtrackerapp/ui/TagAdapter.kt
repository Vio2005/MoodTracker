package com.example.moodtrackerapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.entity.TagEntity
import com.example.moodtrackerapp.databinding.ItemTagBinding

class TagAdapter(
    private val onTagClick: (TagEntity, Boolean) -> Unit
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    private var tags: List<TagEntity> = listOf()
    private var selectedTags: List<TagEntity> = listOf()

    fun submitList(allTags: List<TagEntity>, selected: List<TagEntity>) {
        tags = allTags
        selectedTags = selected
        notifyDataSetChanged()
    }

    inner class TagViewHolder(val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: TagEntity) {
            binding.cbTag.text = tag.name
            binding.cbTag.isChecked = selectedTags.any { it.tagId == tag.tagId }

            binding.cbTag.setOnCheckedChangeListener { _, isChecked ->
                onTagClick(tag, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tags[position])
    }

    override fun getItemCount() = tags.size
}



