package de.mannodermaus.kommandah.views.main.ui

import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.models.InstructionMeta
import de.mannodermaus.kommandah.models.InstructionParam
import de.mannodermaus.kommandah.utils.extensions.addViews
import de.mannodermaus.kommandah.utils.extensions.showDialog

// FIXME This entire file is using kotlin-reflect because of time constraints.
// Usually we'd prefer a cleaner list of available Instructions
// and follow-up-logic, encapsulated as a data class or sth. other than this.

/* Types */

//private class InstructionChoice(val cls: KClass<Instruction>) {
//  val parameters = cls.primaryConstructor?.parameters ?: emptyList()
//  val prettyName: CharSequence by lazy {
//    if (parameters.isEmpty()) {
//      cls.simpleName ?: ""
//
//    } else {
//      // Include parameters in the result
//      SpannableStringBuilder()
//          .append(cls.simpleName)
//          .append(parameters.map { it.name }.joinToString(
//              separator = ", ",
//              prefix = " (",
//              postfix = ")"),
//              StyleSpan(Typeface.ITALIC), Spanned.SPAN_INCLUSIVE_INCLUSIVE)
//    }
//  }
//
//  fun newInstance(vararg args: Any?): Instruction {
//    return if (cls.objectInstance != null) {
//      // e.g. "Print"
//      cls.objectInstance as Instruction
//
//    } else {
//      // e.g. "Push X"
//      cls.primaryConstructor?.call(*args) as Instruction
//    }
//  }
//}
//
///**
// * Mapping between Instruction classes & their choice-related value objects.
// * Evaluated once on demand, since its construction is pretty expensive.
// */
//private val INSTRUCTION_CHOICES_LIST = Instruction::class.nestedClasses
//    .map { InstructionChoice(it as KClass<Instruction>) }

/* Functions */

/**
 * Present the dialog in which the user may select an Instruction to append to their Program.
 * The provided function is invoked upon selecting a choice.
 */
fun showInstructionChooserDialog(context: Context, callback: (Instruction) -> Unit): MaterialDialog {
  val allChoices = Instruction.allOperators()

  return context.showDialog {
    title(R.string.main_dialog_instructionchooser_title)
    items(allChoices)
    autoDismiss(true)
    canceledOnTouchOutside(true)
    itemsCallbackSingleChoice(-1) { _, _, which, _ ->
      // We need a follow-up dialog for Instruction classes
      // that require parameters for construction,
      // and defer this work to a follow-up dialog.
      // Otherwise, the callback is invoked immediately
      val metadata = Instruction.metadataFromOperator(allChoices[which])
      if (metadata.hasParameters) {
        showParameterizedInstructionFollowupDialog(context, metadata, callback = callback)
        false

      } else {
        callback.invoke(metadata.createInstruction())
        true
      }
    }
  }
}

fun showInstructionEditDialog(context: Context, instruction: Instruction, callback: (Instruction) -> Unit): MaterialDialog =
    showParameterizedInstructionFollowupDialog(context, instruction.metadata(), instruction, callback)

/* Private */

private fun showParameterizedInstructionFollowupDialog(context: Context,
                                                       metadata: InstructionMeta,
                                                       initial: Instruction? = null,
                                                       callback: (Instruction) -> Unit): MaterialDialog {
  // For each parameter that the Instruction requires upon creation,
  // insert an EditText into the dialog and require it to be non-empty upon confirmation
  val editTexts = metadata.parameters.map { parameter ->
    TextInputLayout(context).apply {
      addView(TextInputEditText(context).apply {
        // Derive the pre-populated text of the field
        val initialText = when (initial) {
          is Instruction.Push -> initial.argument.toString()
          is Instruction.Call -> initial.address.toString()
          else -> null
        }
        initialText?.let { setText(initialText) }

        // Derive the input type from the parameter type
        inputType = when (parameter) {
          is InstructionParam.Int -> InputType.TYPE_CLASS_NUMBER
          else -> InputType.TYPE_CLASS_TEXT
        }

        // More trivial parameters
        hint = parameter.name
        setSingleLine(true)
      })
    }
  }
  val wrapper = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    addViews(editTexts)
  }

  return context.showDialog {
    title(metadata.operator)
    customView(wrapper, true)
    positiveText(R.string.main_dialog_ok)
    negativeText(R.string.main_dialog_cancel)
    canceledOnTouchOutside(false)
    autoDismiss(false)
    onNegative { dialog, _ -> dialog.dismiss() }
    onPositive { dialog, _ ->
      // Validate & aggregate all input field's values
      // & create the Instruction from them after that.
      val missingField = editTexts.firstOrNull { it.editText!!.text.isEmpty() }
      if (missingField != null) {
        missingField.error = context.getString(R.string.main_dialog_inputrequired)

      } else {
        // Try converting arguments to integers or use the Strings as-is
        val inputValues = editTexts
            .map { it.editText!!.text.toString() }
            .map { it.toIntOrNull() ?: it }
            .toTypedArray()
        val instruction = metadata.createInstruction(*inputValues)
        callback.invoke(instruction)
        dialog.dismiss()
      }
    }
  }
}
