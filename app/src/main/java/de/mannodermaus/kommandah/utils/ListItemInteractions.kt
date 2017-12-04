package de.mannodermaus.kommandah.utils

import android.support.v7.widget.RecyclerView

interface ListItemClickListener<in T> {
  fun handleListItemClick(holder: RecyclerView.ViewHolder, item: T?)
}

interface ListItemDragListener {
  fun startListItemDrag(holder: RecyclerView.ViewHolder)
}

interface ItemTouchHelperAware {
  fun onItemMove(fromPosition: Int, toPosition: Int)
  fun onItemDismiss(position: Int)
}
