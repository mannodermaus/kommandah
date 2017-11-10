package de.mannodermaus.kommandah.utils.widgets

import android.content.Context
import android.support.annotation.Keep
import android.support.design.widget.BottomSheetBehavior
import android.util.AttributeSet
import android.view.View

/**
 * Custom extension for "bottom sheet" Views inside a CoordinatorLayout
 * with multiple callbacks, since that's not supported out of the box.
 *
 * Referenced in XML by class name,
 * hence @Keep is required to be safe from Proguard's wrath.
 */
@Keep
open class CompositeBottomSheetBehavior<V : View>
constructor(context: Context, attrs: AttributeSet)
  : BottomSheetBehavior<V>(context, attrs) {

  private val callbacks = mutableListOf<BottomSheetCallback>()

  private val proxy = object : BottomSheetCallback() {
    override fun onSlide(bottomSheet: View, slideOffset: Float) {
      callbacks.forEach { it.onSlide(bottomSheet, slideOffset) }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
      callbacks.forEach { it.onStateChanged(bottomSheet, newState) }
    }
  }

  init {
    super.setBottomSheetCallback(proxy)
  }

  /* Overrides */

  @Deprecated(
      message = "Use addCallback() instead",
      replaceWith = ReplaceWith("addCallback(callback)"),
      level = DeprecationLevel.ERROR)
  override fun setBottomSheetCallback(callback: BottomSheetCallback) {
  }

  /* Public */

  fun addCallback(callback: BottomSheetCallback) {
    callbacks += callback
  }

  fun removeCallback(callback: BottomSheetCallback) {
    callbacks -= callback
  }
}
