package de.mannodermaus.kommandah.views.main

import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import de.mannodermaus.kommandah.R

/**
 * Hooks in a callback between the RecyclerView & the bottom toolbar,
 * such that the former is being scrolled along with the toolbar expanding.
 */
fun applyMoveAwayBehavior(recyclerView: RecyclerView, bottomToolbar: View) {
  // Hook into the BottomSheetBehavior of the toolbar,
  // obtain some dimension resources & dynamically re-position
  // the RecyclerView's bottom edge so that it always rests on top of
  // the toolbar's top edge.
  val fullHeight = recyclerView.resources.getDimension(R.dimen.main_bottomtoolbar_expanded_height)
  val peekHeight = recyclerView.resources.getDimension(R.dimen.main_bottomtoolbar_collapsed_height)
  val diffHeight = fullHeight - peekHeight

  val cb = object : BottomSheetBehavior.BottomSheetCallback() {
    override fun onSlide(bottomSheet: View, slideOffset: Float) {
      val clp = recyclerView.layoutParams as CoordinatorLayout.LayoutParams
      clp.bottomMargin = (peekHeight + diffHeight * slideOffset).toInt()
      recyclerView.layoutParams = clp
      recyclerView.requestLayout()
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
    }
  }
  BottomSheetBehavior.from(bottomToolbar).setBottomSheetCallback(cb)

  // Invoke manually once to setup the RecyclerView's initial margin
  cb.onSlide(bottomToolbar, 0.0f)
}
