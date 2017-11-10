package de.mannodermaus.kommandah.views.main

import de.mannodermaus.kommandah.models.Instruction

/**
 * Representation of a single "list change" event in a Program, exposed to the View layer.
 * It holds some information about the diff compared to the last event.
 */
@Deprecated(message = "ViewModel is already in charge of the different events; move to UI-bound model class")
sealed class InstructionListEvent(val instructions: List<Instruction>) {
  class New(instructions: List<Instruction>) : InstructionListEvent(instructions)
  class Swap(val fromPosition: Int, val toPosition: Int, instructions: List<Instruction>) : InstructionListEvent(instructions)
  class Remove(val position: Int, instructions: List<Instruction>) : InstructionListEvent(instructions)
}

/**
 * Different states that the execution of a Program can be in.
 */
enum class ExecutionStatus { PAUSED, RUNNING }

/**
 * The different kinds of events piped through to a Console window.
 */
sealed class ConsoleEvent {
  object Clear : ConsoleEvent()
  data class Started(val numLines: Int) : ConsoleEvent()
  data class Message(val line: Int, val message: String) : ConsoleEvent()
  data class Error(val cause: Throwable, val instruction: Instruction?) : ConsoleEvent()
  object Finished : ConsoleEvent()
}
