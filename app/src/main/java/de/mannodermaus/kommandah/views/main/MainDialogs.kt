package de.mannodermaus.kommandah.views.main

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import com.afollestad.materialdialogs.MaterialDialog
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.models.Instruction
import timber.log.Timber
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

// TODO This is using kotlin-reflect because of time constraints.
// Usually we'd prefer a cleaner list of available Instructions
// and follow-up-logic
private class InstructionChoice(val cls: KClass<*>) {
  val parameters = cls.primaryConstructor?.parameters ?: emptyList()
  val prettyName by lazy {
    if (parameters.isEmpty()) {
      cls.simpleName

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
      cls.primaryConstructor?.call(args) as Instruction
    }
  }
}

fun showInstructionChooserDialog(context: Context, callback: (Instruction) -> Unit): MaterialDialog {
  val instructionClasses = Instruction::class.nestedClasses
      .map(::InstructionChoice)

  val builder = MaterialDialog.Builder(context).apply {
    title(R.string.main_dialog_instructionchooser_title)
    items(instructionClasses.map { it.prettyName })
    autoDismiss(true)
    canceledOnTouchOutside(true)
    itemsCallbackSingleChoice(-1) { _, _, which, _ ->
      val selected = instructionClasses[which]
      Timber.i("Selected: $selected")
      if (selected.parameters.isNotEmpty()) {
        TODO("Follow-up for instructions with parameters")

      } else {
        callback.invoke(selected.newInstance())
      }
      true
    }
  }

  return builder.show()
}
