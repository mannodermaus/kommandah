package de.mannodermaus.kommandah.utils.extensions

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StyleRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
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
