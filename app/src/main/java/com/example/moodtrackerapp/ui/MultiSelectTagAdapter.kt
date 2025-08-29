package com.example.moodtrackerapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.entity.TagEntity

class MultiSelectTagAdapter(
    val tags: List<TagEntity>,
    private val selectedTags: MutableSet<TagEntity> = mutableSetOf(), // Use Set to avoid duplicates
    private val onTagSelected: (TagEntity, Boolean) -> Unit
) : RecyclerView.Adapter<MultiSelectTagAdapter.TagViewHolder>() {

    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.cbTag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag_checkbox, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]

        // Prevent triggering listener during recycling
        holder.checkBox.setOnCheckedChangeListener(null)

        holder.checkBox.text = tag.name
        holder.checkBox.isChecked = selectedTags.contains(tag)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedTags.add(tag)
            else selectedTags.remove(tag)

            onTagSelected(tag, isChecked)
        }
    }

    override fun getItemCount(): Int = tags.size

    // Preselect tags
    fun preselectTags(preselected: List<TagEntity>) {
        selectedTags.clear()
        selectedTags.addAll(preselected.distinctBy { it.tagId }) // Remove duplicates
        notifyDataSetChanged()
    }

    // Get selected tags as a list
    fun getSelectedTags(): List<TagEntity> = selectedTags.toList()
}
