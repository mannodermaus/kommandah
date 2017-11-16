package de.mannodermaus.kommandah.views.main.models

import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.models.OrderedMap
import de.mannodermaus.kommandah.models.ProgramException

/**
 * UI-facing wrapper model for an item represented in the Activity's list of Instructions.
 * This contains some UI-relevant info about how to render the particular cell as well.
 */
data class InstructionItem(val instruction: Instruction, val state: State = State.NONE) {
  /**
   * The Instruction was either:
   * - not executed at all (NONE)
   * - executed successfully (SUCCESS)
   * - executed & raised an error (ERROR)
   */
  enum class State { NONE, SUCCESS, ERROR }
}

/**
 * The state of the current program held inside the ViewModel.
 * This holds the internal representation of a "session" inside the app
 */
data class ProgramState(
    val savedId: Long? = null,
    val title: String? = null,
    val instructions: OrderedMap<InstructionItem> = OrderedMap(),
    val running: Boolean = false)

/**
 * Different states that the execution of a Program can be in.
 * Unlike [ProgramState], this class is exposed to the View via the ViewModel.
 */
data class ExecutionState(
    /** Whether or not the current program is being executed */
    val running: Boolean,
    /** The title of the currently loaded Program, if any */
    val programTitle: String?)

/**
 * The different kinds of events piped through to a Console window.
 */
sealed class ConsoleEvent {
  object Clear : ConsoleEvent()
  data class Started(val numLines: Int) : ConsoleEvent()
  data class Message(val line: Int, val message: String) : ConsoleEvent()
  data class Error(val cause: ProgramException, val instruction: Instruction?) : ConsoleEvent()
  object Finished : ConsoleEvent()
}
