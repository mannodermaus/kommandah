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
        showParametrizedInstructionFollowupDialog(context, metadata, callback = callback)
        false

      } else {
        callback.invoke(metadata.createInstruction())
        true
      }
    }
  }
}

fun showInstructionEditDialog(context: Context, instruction: Instruction, callback: (Instruction) -> Unit): MaterialDialog =
    showParametrizedInstructionFollowupDialog(context, instruction.metadata, instruction, callback)

/* Private */

private fun showParametrizedInstructionFollowupDialog(context: Context,
                                                      metadata: InstructionMeta,
                                                      initial: Instruction? = null,
                                                      callback: (Instruction) -> Unit): MaterialDialog {
  // For each parameter that the Instruction requires upon creation,
  // insert an EditText into the dialog and require it to be non-empty upon confirmation
  val editTexts = metadata.parameterTypes.map { parameter ->
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
        val inputValues: Array<Any> = editTexts
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
