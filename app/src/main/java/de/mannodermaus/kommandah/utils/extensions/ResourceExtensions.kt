package de.mannodermaus.kommandah.utils.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat

/**
 * Obtain a Drawable in an API-level-safe manner, optionally tinting it with the provided color.
 */
fun Context.getDrawableCompat(@DrawableRes drawable: Int, @ColorRes tint: Int? = 0): Drawable {
  // Get base Drawable, then apply tint if necessary
  var d = ResourcesCompat.getDrawable(resources, drawable, theme)!!
  if (tint != null) {
    d = d.tinted(getColorCompat(tint))
  }
  return d
}

/**
 * Obtain a Color in an API-level-safe manner.
 */
@ColorInt
fun Context.getColorCompat(@ColorRes res: Int): Int =
    ResourcesCompat.getColor(resources, res, theme)

/**
 * Returns a copy of this Drawable, tinted with the provided color.
 */
fun Drawable.tinted(@ColorInt color: Int): Drawable {
  val wrapped = DrawableCompat.wrap(this)
  DrawableCompat.setTint(wrapped, color)
  return wrapped
}
