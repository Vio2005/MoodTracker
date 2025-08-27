package com.example.moodtrackerapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.entity.TagEntity

class MultiSelectTagAdapter(
    private val tags: List<TagEntity>,
    private val onTagSelected: (TagEntity, Boolean) -> Unit
) : RecyclerView.Adapter<MultiSelectTagAdapter.TagViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag_checkbox, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.checkBox.text = tag.name
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onTagSelected(tag, isChecked)
        }
    }

    override fun getItemCount(): Int = tags.size

    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.cbTag)
    }
}
