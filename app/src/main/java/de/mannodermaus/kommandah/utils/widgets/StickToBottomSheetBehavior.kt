package de.mannodermaus.kommandah.utils.widgets

import android.content.Context
import android.support.annotation.Keep
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import de.mannodermaus.kommandah.R

/**
 * Custom CoordinatorLayout.Behavior implementation.
 *
 * It basically connects a [RecyclerView] to the View with this behavior,
 * scrolling it upward whenever the bottom sheet is being moved.
 *
 * To use this, obtain an instance in your View & set it up via [attachToRecyclerView].
 *
 * Referenced in XML by class name,
 * hence the @Keep annotation to ensure that Proguard doesn't mess us up.
 */
@Keep
class StickToBottomSheetBehavior
  constructor(context: Context, attrs: AttributeSet)
  : CompositeBottomSheetBehavior<View>(context, attrs) {

  fun attachToRecyclerView(view: View, recyclerView: RecyclerView) {

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
    addCallback(cb)

    // Invoke manually once to setup the RecyclerView's initial margin
    cb.onSlide(view, 0.0f)
  }

  companion object {
    @JvmStatic
    fun from(view: View) = BottomSheetBehavior.from(view) as StickToBottomSheetBehavior
  }
}
