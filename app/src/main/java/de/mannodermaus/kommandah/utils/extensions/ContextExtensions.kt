package de.mannodermaus.kommandah.utils.extensions

import android.content.Context
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import de.mannodermaus.kommandah.R

/**
 * Quick-hand function to configure and show a Dialog window.
 */
inline fun Context.showDialog(config: MaterialDialog.Builder.() -> Unit): MaterialDialog {
  val builder = MaterialDialog.Builder(this).apply {
    // Default configuration
    positiveColorRes(R.color.primary)
    negativeColorRes(R.color.accent)
  }
  config.invoke(builder)
  return builder.show()
}

fun Context.longToast(message: CharSequence) {
  Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
