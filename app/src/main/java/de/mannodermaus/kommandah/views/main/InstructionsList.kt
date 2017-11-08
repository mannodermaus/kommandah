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
import timber.log.Timber
import java.util.*

private val itemLayoutResource = R.layout.list_item_instruction

/* Adapter */

/**
 * Adapter implementation for the RecyclerView used on the main screen.
 */
class InstructionAdapter(private val dragListener: ListItemDragListener)
  : RecyclerView.Adapter<InstructionViewHolder>(),
    ItemTouchHelperAware {

  private var items: MutableList<Instruction> = mutableListOf()

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

  override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
    Collections.swap(items, fromPosition, toPosition)
    notifyItemMoved(fromPosition, toPosition)
    return true
  }

  override fun onItemDismiss(position: Int) {
    items.removeAt(position)
    notifyItemRemoved(position)
  }

  fun update(newItems: List<Instruction>) {
    // Diff the current list against the new one & notify accordingly
    val oldItems = items
    items = newItems.toMutableList()

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
class InstructionViewHolder(view: View, dragListener: ListItemDragListener)
  : RecyclerView.ViewHolder(view) {

  init {
    // Allow drag-and-drop for cells
    itemView.ivDragHandle.setOnTouchListener(object : View.OnTouchListener {
      override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
          dragListener.startDrag(this@InstructionViewHolder)
        }
        return false
      }
    })
  }

  fun bind(item: Instruction) {
    itemView.tvNumber.text = adapterPosition.toString()
    itemView.tvInstructionName.text = item.describe()
  }
}
