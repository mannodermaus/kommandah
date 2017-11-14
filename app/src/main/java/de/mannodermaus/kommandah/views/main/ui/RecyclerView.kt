package de.mannodermaus.kommandah.views.main.ui

import android.animation.ObjectAnimator
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.utils.ItemTouchHelperAware
import de.mannodermaus.kommandah.utils.ListItemClickListener
import de.mannodermaus.kommandah.utils.ListItemDragListener
import de.mannodermaus.kommandah.views.main.models.InstructionItem
import kotlinx.android.synthetic.main.list_item_instruction.view.*

/* Constants */

private val itemLayoutResource = R.layout.list_item_instruction
private val shakeAnimationValues = floatArrayOf(50f, -50f, 25f, -25f, 12f, -12f, 6f, -6f, 0f)

/* Adapter */

/**
 * Adapter implementation for the RecyclerView used on the main screen.
 */
class InstructionAdapter(
    private val clickListener: ListItemClickListener<InstructionItem>,
    private val dragListener: ListItemDragListener)
  : RecyclerView.Adapter<InstructionViewHolder>(),
    ItemTouchHelperAware {

  private var items: List<InstructionItem?> = emptyList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructionViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return InstructionViewHolder(
        view = inflater.inflate(itemLayoutResource, parent, false),
        clickListener = clickListener,
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

  fun update(newItems: List<InstructionItem?>) {
    // Diff the current list against the new one & notify accordingly
    val oldItems = items
    items = newItems

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
class InstructionViewHolder(view: View,
                            clickListener: ListItemClickListener<InstructionItem>,
                            dragListener: ListItemDragListener)
  : RecyclerView.ViewHolder(view) {

  private var item: InstructionItem? = null

  init {
    itemView.setOnClickListener {
      clickListener.handleListItemClick(this, item)
    }

    // Allow drag-and-drop for cells, but only those that actually "exist"
    itemView.ivDragHandle.setOnTouchListener { _, event ->
      item?.let {
        when (event.actionMasked) {
          MotionEvent.ACTION_DOWN -> dragListener.startListItemDrag(this@InstructionViewHolder)
        }
      }
      false
    }
  }

  fun bind(item: InstructionItem?) {
    this.item = item

    if (item != null) {
      bindActual(item)
    } else {
      bindPlaceholder()
    }
  }

  /**
   * Bind an actual, existing instruction to this ViewHolder
   */
  private fun bindActual(item: InstructionItem) {
    itemView.tvNumber.text = adapterPosition.toString()
    itemView.tvInstructionName.text = item.instruction.toString()
    itemView.alpha = 1.0f

    // Decide the status indicator based on the item's properties
    val colorRes = if (item.instruction is Instruction.Stop) {
      R.drawable.bg_instruction_stop
    } else when (item.state) {
      InstructionItem.State.NONE -> R.color.main_instruction_none
      InstructionItem.State.SUCCESS -> R.color.main_instruction_success
      InstructionItem.State.ERROR -> R.color.main_instruction_error
    }
    itemView.status.setBackgroundResource(colorRes)

    // Shake the item on errors
    if (item.state == InstructionItem.State.ERROR) {
      shake()
    }
  }

  /**
   * Bind the placeholder to to this ViewHolder
   */
  private fun bindPlaceholder() {
    itemView.tvNumber.text = adapterPosition.toString()
    itemView.tvInstructionName.text = ""
    itemView.status.setBackgroundColor(0x00000000)
    itemView.alpha = 0.4f
  }

  private fun shake() {
    ObjectAnimator.ofFloat(itemView, "translationX", *shakeAnimationValues)
        .setDuration(300 /* ms */)
        .start()
  }
}
