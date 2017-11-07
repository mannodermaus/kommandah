package de.mannodermaus.kommandah.views.main

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.mannodermaus.kommandah.R
import kotlinx.android.synthetic.main.list_item_instruction.view.*

private val itemLayoutResource = R.layout.list_item_instruction

/**
 * Adapter implementation for the RecyclerView used on the main screen.
 */
class InstructionAdapter : RecyclerView.Adapter<InstructionViewHolder>() {

  private var items: List<InstructionData> = emptyList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructionViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return InstructionViewHolder(inflater.inflate(itemLayoutResource, parent, false))
  }

  override fun onBindViewHolder(holder: InstructionViewHolder, position: Int) {
    val item = items[position]
    holder.bind(item)
  }

  override fun getItemCount(): Int = items.size

  fun update(newItems: List<InstructionData>) {
    // Diff the current list against the new one & notify accordingly
    val oldItems = items
    items = newItems

    val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
      override fun getOldListSize(): Int = oldItems.size
      override fun getNewListSize(): Int = newItems.size
      override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
          oldItems[oldItemPosition] == newItems[newItemPosition]
      override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
          oldItems[oldItemPosition] == newItems[newItemPosition]
    }, true)
    diff.dispatchUpdatesTo(this)
  }
}

/**
 * ViewHolder implementation for a cell in the RecyclerView used on the main screen.
 */
class InstructionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

  fun bind(item: InstructionData) {
    itemView.tvNumber.text = item.index.toString()

    val instruction = item.instruction
    if (instruction != null) {
      itemView.tvInstructionName.text = instruction.describe()
      itemView.alpha = 1.0f
    } else {
      itemView.tvInstructionName.text = ""
      itemView.alpha = 0.5f
    }
  }
}
