package com.example.moodtrackerapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodtrackerapp.data.entity.TagEntity
import com.example.moodtrackerapp.databinding.ItemTagBinding

class TagAdapter(
    private val onTagClick: (TagEntity, Boolean) -> Unit
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    private var tags: List<TagEntity> = listOf()
    private var selectedTags: Set<Long> = emptySet() // store selected tagIds for fast lookup

    fun submitList(allTags: List<TagEntity>, selected: List<TagEntity>) {
        tags = allTags
        selectedTags = selected.map { it.tagId }.toSet()
        notifyDataSetChanged()
    }

    inner class TagViewHolder(private val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: TagEntity) {
            // Remove previous listener to prevent recycling issues
            binding.cbTag.setOnCheckedChangeListener(null)

            binding.cbTag.text = tag.name
            binding.cbTag.isChecked = selectedTags.contains(tag.tagId)

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

    override fun getItemCount(): Int = tags.size
}
