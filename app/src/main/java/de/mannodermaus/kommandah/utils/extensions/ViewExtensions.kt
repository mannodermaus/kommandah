package de.mannodermaus.kommandah.utils.extensions

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.annotation.StyleRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import de.mannodermaus.kommandah.R

/* Toolbar */

fun Toolbar.setTitleTextAppearance(@StyleRes res: Int) =
    this.setTitleTextAppearance(context, res)

/**
 * Combination of the two entry points for
 * Toolbar configuration of an Activity.
 * This removes the need to access both the
 * SupportActionBar and Toolbar APIs.
 */
class ToolbarConfig
internal constructor(val activity: AppCompatActivity) {
  private val supportActionBar by lazy { activity.supportActionBar }
  private val toolbar by lazy { activity.findViewById<Toolbar>(R.id.action_bar) }

  fun showHome(state: Boolean) {
    supportActionBar?.apply {
      setDisplayShowHomeEnabled(state)
      setDisplayHomeAsUpEnabled(state)
    }
  }

  fun setHomeIcon(@DrawableRes res: Int, @ColorRes tint: Int? = 0) {
    supportActionBar?.apply {
      setHomeAsUpIndicator(activity.getDrawableCompat(res, tint))
    }
  }

  fun setTitleTextAppearance(@StyleRes res: Int) {
    toolbar?.setTitleTextAppearance(res)
  }
}

fun AppCompatActivity.toolbar(config: ToolbarConfig.() -> Unit) {
  config.invoke(ToolbarConfig(this))
}

/* TextView */

fun TextView.appendLine(line: CharSequence = "") = this.also {
  append(line)
  append("\n")
}

fun TextView.appendLine(@StringRes res: Int, vararg args: Any) = this.also {
  append(resources.getString(res, *args))
  append("\n")
}

/* BottomSheetBehavior */

private val <V : View> BottomSheetBehavior<V>.compositeCallback: BottomSheetBehavior.BottomSheetCallback by lazy {
  object : BottomSheetBehavior.BottomSheetCallback() {
    override fun onSlide(bottomSheet: View, slideOffset: Float) {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

  }
}
