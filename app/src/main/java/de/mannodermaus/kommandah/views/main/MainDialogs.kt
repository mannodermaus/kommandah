package de.mannodermaus.kommandah.views.main

import android.content.Context
import android.graphics.Typeface
import android.support.design.widget.TextInputEditText
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.utils.extensions.addViews
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

// FIXME This is using kotlin-reflect because of time constraints.
// Usually we'd prefer a cleaner list of available Instructions
// and follow-up-logic, encapsulated as a data class or sth. other than this.
private class InstructionChoice(val cls: KClass<*>) {
  val parameters = cls.primaryConstructor?.parameters ?: emptyList()
  val prettyName: CharSequence by lazy {
    if (parameters.isEmpty()) {
      cls.simpleName ?: ""

    } else {
      // Include parameters in the result
      SpannableStringBuilder()
          .append(cls.simpleName)
          .append(parameters.map { it.name }.joinToString(
              separator = ", ",
              prefix = " (",
              postfix = ")"),
              StyleSpan(Typeface.ITALIC), Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    }
  }

  fun newInstance(vararg args: Any?): Instruction {
    return if (cls.objectInstance != null) {
      // e.g. "Print"
      cls.objectInstance as Instruction

    } else {
      // e.g. "Push X"
      cls.primaryConstructor?.call(*args) as Instruction
    }
  }
}

/**
 * Present the dialog in which the user may select an Instruction to append to their Program.
 * The provided function is invoked upon selecting a choice.
 */
fun showInstructionChooserDialog(context: Context, callback: (Instruction) -> Unit): MaterialDialog {
  val instructionClasses = Instruction::class.nestedClasses
      .map(::InstructionChoice)

  val builder = MaterialDialog.Builder(context).apply {
    title(R.string.main_dialog_instructionchooser_title)
    items(instructionClasses.map { it.prettyName })
    autoDismiss(true)
    canceledOnTouchOutside(true)
    itemsCallbackSingleChoice(-1) { _, _, which, _ ->
      // We need a follow-up dialog for Instruction classes
      // that require parameters for construction,
      // and defer this work to a follow-up dialog.
      // Otherwise, the callback is invoked immediately
      val selected = instructionClasses[which]
      if (selected.parameters.isNotEmpty()) {
        showParameterizedInstructionFollowupDialog(context, selected, callback)
        false

      } else {
        callback.invoke(selected.newInstance())
        true
      }
    }
  }

  return builder.show()
}

private fun showParameterizedInstructionFollowupDialog(context: Context,
                                                       choice: InstructionChoice,
                                                       callback: (Instruction) -> Unit): MaterialDialog {
  // For each parameter that the Instruction requires upon creation,
  // insert an EditText into the dialog and require it to be non-empty upon confirmation
  val editTexts = choice.parameters.map { parameter ->
    TextInputEditText(context).apply {
      hint = parameter.name
      setSingleLine(true)
    }
  }
  val wrapper = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    addViews(editTexts)
  }

  val builder = MaterialDialog.Builder(context).apply {
    title(choice.prettyName)
    customView(wrapper, true)
    positiveText(R.string.main_dialog_ok)
    negativeText(R.string.main_dialog_cancel)
    canceledOnTouchOutside(false)
    autoDismiss(false)
    onNegative { dialog, _ -> dialog.dismiss() }
    onPositive { dialog, _ ->
      // Validate & aggregate all input field's values
      // & create the Instruction from them after that.
      val missingField = editTexts.firstOrNull { it.text.isEmpty() }
      if (missingField != null) {
        // TODO Naming
        missingField.error = "LOL NO"

      } else {
        // Try converting arguments to integers or use the Strings as-is
        val inputValues = editTexts
            .map { it.text.toString() }
            .map { it.toIntOrNull() ?: it }
            .toTypedArray()
        val instruction = choice.newInstance(*inputValues)
        callback.invoke(instruction)
        dialog.dismiss()
      }
    }
  }

  return builder.show()
}
