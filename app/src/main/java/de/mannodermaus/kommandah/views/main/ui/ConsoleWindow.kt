package de.mannodermaus.kommandah.views.main.ui

import android.text.Editable
import android.widget.ScrollView
import android.widget.TextView
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.utils.SimpleTextWatcher
import de.mannodermaus.kommandah.utils.extensions.appendLine
import de.mannodermaus.kommandah.views.main.models.ConsoleEvent

/**
 * Handler for incoming [ConsoleEvent],
 * pushed into a given [TextView].
 */
class ConsoleWindow(val view: TextView) {

  init {
    // Validate the TextView that was passed in
    // (it is required to be located inside a ScrollView)
    val parent = view.parent as? ScrollView
        ?: throw IllegalStateException("TextView passed to Console needs to be wrapped inside ScrollView")

    // Automatically scroll to the bottom when new text arrives
    view.addTextChangedListener(object : SimpleTextWatcher() {
      override fun afterTextChanged(text: Editable) {
        parent.fullScroll(ScrollView.FOCUS_DOWN)
      }
    })
  }

  fun handle(event: ConsoleEvent) {
    when (event) {
      is ConsoleEvent.Clear ->
        view.text = ""

      is ConsoleEvent.Started ->
        view.appendLine(R.string.main_console_started, event.numLines)
            .appendLine(R.string.main_console_separator)
            .appendLine()

      is ConsoleEvent.Message ->
        view.appendLine(R.string.main_console_log, event.line, event.message)

      is ConsoleEvent.Finished ->
        view.appendLine()
            .appendLine(R.string.main_console_separator)
            .appendLine(R.string.main_console_finished)

      is ConsoleEvent.Error ->
        view
            .appendLine()
            .appendLine(R.string.main_console_separator)
            .appendLine(R.string.main_console_error)
            .appendLine(event.cause.message.toString())
    }
  }
}
