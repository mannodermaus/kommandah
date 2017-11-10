package de.mannodermaus.kommandah.views.main

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.utils.ItemTouchHelperAware
import de.mannodermaus.kommandah.utils.ListItemDragListener
import kotlinx.android.synthetic.main.list_item_instruction.view.*
import java.util.*

private val itemLayoutResource = R.layout.list_item_instruction

/* Adapter */

/**
 * Adapter implementation for the RecyclerView used on the main screen.
 */
class InstructionAdapter(private val dragListener: ListItemDragListener)
  : RecyclerView.Adapter<InstructionViewHolder>(),
    ItemTouchHelperAware {

  private var _items: List<Instruction> = emptyList()
  val items: List<Instruction>
    get() = Collections.unmodifiableList(_items)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructionViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return InstructionViewHolder(
        view = inflater.inflate(itemLayoutResource, parent, false),
        dragListener = dragListener)
  }

  override fun onBindViewHolder(holder: InstructionViewHolder, position: Int) {
    val item = items[position]
    holder.bind(item)
  }

  override fun getItemCount(): Int = items.size

  /* Interactions */

  override fun onItemMove(fromPosition: Int, toPosition: Int) {
    notifyItemMoved(fromPosition, toPosition)
  }

  override fun onItemDismiss(position: Int) {
    notifyItemRemoved(position)
  }

  fun update(newItems: List<Instruction>) {
    // Diff the current list against the new one & notify accordingly
    val oldItems = items
    _items = newItems

    val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
      override fun getOldListSize(): Int = oldItems.size
      override fun getNewListSize(): Int = newItems.size
      override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
          oldItems[oldItemPosition] == newItems[newItemPosition]
      override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
          oldItems[oldItemPosition] == newItems[newItemPosition] && oldItemPosition == newItemPosition
    }, true)
    diff.dispatchUpdatesTo(this)
  }
}

/**
 * ViewHolder implementation for a cell in the RecyclerView used on the main screen.
 */
class InstructionViewHolder(view: View, dragListener: ListItemDragListener)
  : RecyclerView.ViewHolder(view) {

  init {
    // Allow drag-and-drop for cells
    itemView.ivDragHandle.setOnTouchListener { _, event ->
      when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> dragListener.startDrag(this@InstructionViewHolder)
      }
      false
    }
  }

  fun bind(item: Instruction) {
    itemView.tvNumber.text = adapterPosition.toString()
    itemView.tvInstructionName.text = item.describe()
  }
}
