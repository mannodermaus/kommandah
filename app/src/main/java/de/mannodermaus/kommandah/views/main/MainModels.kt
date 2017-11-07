package de.mannodermaus.kommandah.views.main

import de.mannodermaus.kommandah.models.Instruction

/**
 * Representation of a single entry in a Program, exposed to the View layer.
 * Note that the included [Instruction] might be absent in cases where
 * there are "empty cells" in the program in-between routines or blocks.
 */
data class InstructionData(val index: Int, val instruction: Instruction?)

/**
 * Different states that the execution of a Program can be in.
 */
enum class ExecutionStatus { PAUSED, RUNNING }

sealed class ConsoleEvent {
  /**
   * Clear Console
   */
  object Clear : ConsoleEvent()

  /**
   * Message to print in the Console
   */
  data class Message(val message: String) : ConsoleEvent()
}
