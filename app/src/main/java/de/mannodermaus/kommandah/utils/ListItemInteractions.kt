package de.mannodermaus.kommandah.utils

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

interface ListItemDragListener {
  fun startDrag(holder: RecyclerView.ViewHolder)
}

interface ItemTouchHelperAware {
  fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
  fun onItemDismiss(position: Int)
}

/**
 * Simple implementation of drag-and-drop & swipe-to-dismiss for RecyclerView items.
 */
class ItemTouchHelperDragCallback(private val delegate: ItemTouchHelperAware) : ItemTouchHelper.Callback() {

  override fun isItemViewSwipeEnabled(): Boolean = true
  override fun isLongPressDragEnabled(): Boolean = true

  override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
    // This assumes a linear LayoutManager
    val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
    val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
    return makeMovementFlags(dragFlags, swipeFlags)
  }

  override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
    if (source.itemViewType != target.itemViewType) {
      return false
    }
    delegate.onItemMove(source.adapterPosition, target.adapterPosition)
    return true
  }

  override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
    delegate.onItemDismiss(holder.adapterPosition)
  }
}
