package de.mannodermaus.kommandah.views.main.ui

import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.models.InstructionMeta
import de.mannodermaus.kommandah.models.InstructionParam
import de.mannodermaus.kommandah.utils.extensions.addViews
import de.mannodermaus.kommandah.utils.extensions.showDialog

/* Types */

/**
 * Invoked after successful creation of an Instruction using the dialog.
 *
 * Parameters:
 * - Instruction  -> The created Instruction
 */
typealias OnInstructionCreated = (Instruction) -> Unit

/**
 * Invoked upon applying changes to the provided Instruction using the Edit dialog.
 * The additional Int parameters indicate
 * if the item is supposed to be moved to a new location in the Program.
 *
 * Parameters:
 * - Instruction  -> The updated instruction
 * - Int          -> The position at which the instruction was present in the Program
 * - Int?         -> The potential new position to which the instruction is moving with this edit
 */
typealias OnInstructionUpdated = (Instruction, Int, Int?) -> Unit

/* Functions */

/**
 * Present the dialog in which the user may select an Instruction to append to their Program.
 * The provided function is invoked upon selecting a choice.
 */
fun showCreateInstructionDialog(context: Context, callback: OnInstructionCreated): MaterialDialog {
  val allChoices = Instruction.allOperators()

  return context.showDialog {
    title(R.string.main_dialog_instructionchooser_title)
    items(allChoices)
    autoDismiss(true)
    canceledOnTouchOutside(true)
    itemsCallbackSingleChoice(-1) { _, _, which, _ ->
      // We need a follow-up dialog to prompt for the desired index
      // at which to place the instruction, in case the instruction
      // requires additional parameters.
      val metadata = Instruction.metadataFromOperator(allChoices[which])
      if (metadata.hasParameters) {
        showParametrizedInstructionFollowupDialog(context, null, metadata) { instruction, _ ->
          callback.invoke(instruction)
        }
        false

      } else {
        callback.invoke(metadata.createInstruction())
        true
      }
    }
  }
}

/**
 * Present the dialog in which the user may edit an Instruction in their Program.
 * The provided function is invoked upon successfully applying the edit.
 */
fun showEditInstructionDialog(
    context: Context,
    position: Int,
    instruction: Instruction,
    callback: OnInstructionUpdated): MaterialDialog =
    showParametrizedInstructionFollowupDialog(context, position,
        instruction.metadata, instruction) { newInstruction, newPosition ->
      // Propagate the new state to the callback
      callback.invoke(newInstruction, position, newPosition)
    }

/* Private */

private fun showParametrizedInstructionFollowupDialog(context: Context,
                                                      position: Int?,
                                                      metadata: InstructionMeta,
                                                      initial: Instruction? = null,
                                                      callback: (Instruction, Int?) -> Unit): MaterialDialog {
  // For each additional parameter that the Instruction requires upon creation,
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

  // The positional argument Field always comes first,
  // if any position is provided already (i.e. "edit mode")
  var indexField: TextInputLayout? = null
  if (position != null) {
    indexField = TextInputLayout(context).apply {
      addView(TextInputEditText(context).apply {
        inputType = InputType.TYPE_CLASS_NUMBER
        hint = context.getString(R.string.main_dialog_instructionline)
        setText(position.toString())
      })
    }
    wrapper.addView(indexField, 0)

    val explanationView = TextView(context).apply {
      setText(R.string.main_dialog_instructionline_explanation)
    }
    wrapper.addView(explanationView, 1)
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
        val newPosition = indexField?.editText?.text.toString().toIntOrNull()

        callback.invoke(instruction, newPosition)
        dialog.dismiss()
      }
    }
  }
}
