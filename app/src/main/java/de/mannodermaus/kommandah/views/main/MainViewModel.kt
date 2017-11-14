package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModel
import de.mannodermaus.kommandah.managers.runtime.Interpreter
import de.mannodermaus.kommandah.models.ExecutionEnvironment
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.models.OrderedMap
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramOutput
import de.mannodermaus.kommandah.utils.extensions.async
import de.mannodermaus.kommandah.views.main.models.ConsoleEvent
import de.mannodermaus.kommandah.views.main.models.ExecutionStatus
import de.mannodermaus.kommandah.views.main.models.InstructionItem
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class MainViewModel
@Inject constructor(private val interpreter: Interpreter) : ViewModel() {

  /**
   * Stream of events related to the Instructions that make up the current Program
   */
  private val instructions: BehaviorSubject<OrderedMap<InstructionItem>> =
      BehaviorSubject.createDefault(OrderedMap())

  /**
   * Stream of events related to the current execution status of the current Program
   */
  private val executionStatus: BehaviorSubject<ExecutionStatus> =
      BehaviorSubject.createDefault(ExecutionStatus.PAUSED)

  /**
   * Stream of events related to handling console messages during execution
   */
  private val consoleMessages: BehaviorSubject<ConsoleEvent> =
      BehaviorSubject.create()

  private val subscriptions: CompositeDisposable = CompositeDisposable()

  /* Lifecycle */

  override fun onCleared() {
    super.onCleared()
    subscriptions.clear()
  }

  /* Streams; doubled from properties to hide BehaviorSubjects from consumers */

  fun instructions(): Observable<List<InstructionItem?>> =
      instructions.map { it.nullPaddedValues() }

  fun executionStatus(): Observable<ExecutionStatus> = executionStatus

  fun consoleMessages(): Observable<ConsoleEvent> = consoleMessages

  /* Interactions */

  fun runProgram() {
    // Compile the instructions into a Program
    val program = Program(instructions.value.nullPaddedValues()
        .withIndex()
        .associate { it.index to it.value?.instruction })

    // Interpret the program, passing through events as side-effects
    subscriptions += interpreter.execute(program, ExecutionEnvironment())
        .async()
        .doOnSubscribe {
          // Update the execution status
          clearInstructionState()
          executionStatus.onNext(ExecutionStatus.RUNNING)
          consoleMessages.onNext(ConsoleEvent.Clear)
        }
        .doOnNext {
          // Handle intermediate item updates
          when (it) {
            is ProgramOutput.Step -> updateInstructionStatus(it.line, InstructionItem.State.SUCCESS)
            is ProgramOutput.Error -> updateInstructionStatus(it.line, InstructionItem.State.ERROR)
          }
        }
        .doOnNext {
          // Handle Console Output
          when (it) {
            is ProgramOutput.Started -> consoleMessages.onNext(ConsoleEvent.Started(it.numLines))
            is ProgramOutput.Step -> it.message?.let { msg -> consoleMessages.onNext(ConsoleEvent.Message(it.line, msg)) }
            is ProgramOutput.Error -> consoleMessages.onNext(ConsoleEvent.Error(it.cause, it.instruction))
            is ProgramOutput.Completed -> consoleMessages.onNext(ConsoleEvent.Finished)
          }
        }
        .doOnTerminate {
          // Reset the execution status
          executionStatus.onNext(ExecutionStatus.PAUSED)
        }
        .subscribe()
  }

  /**
   * Appends the given instruction to the end of the Program,
   * then fires a notification to subscribers of the data.
   */
  fun addInstruction(instruction: Instruction) {
    updateItemsInternal { it += InstructionItem(instruction) }
  }

  /**
   * Update the given instruction at the provided index, if any,
   * then fires a notification to subscribers of the data.
   * If no positional index is given, the instruction is appended to the end.
   */
  fun updateInstruction(instruction: Instruction, position: Int, moveToPosition: Int?) {
    updateItemsInternal {
      if (moveToPosition != null) {
        // An existing item is moving to a new position.
        // Delete the current one at the old position first
        val oldItem = it.remove(position)!!
        it.insertBefore(moveToPosition, oldItem.copy(instruction = instruction))

      } else {
        // The item remained at the same position; simply replace
        it[position] = it.getValue(position).copy(instruction = instruction)
      }
    }
  }

  /**
   * Swaps the instructions at the given positions with each other,
   * then fires a notification to subscribers of the data.
   */
  fun swapInstructions(fromPosition: Int, toPosition: Int) {
    updateItemsInternal {
      it.swap(fromPosition, toPosition)
    }
  }

  /**
   * Removes the instruction at the given position,
   * then fires a notification to subscribers of the data.
   */
  fun removeInstruction(position: Int) {
    updateItemsInternal { it.remove(position) }
  }

  fun hasInstructionAt(position: Int): Boolean = instructions.value.containsKey(position)

  /* Private */

  /**
   * Reset all items' "State" to NONE.
   * This is invoked upon executing a Program to clear all UI hints
   * related to success or error for each instruction.
   */
  private fun clearInstructionState() {
    updateItemsInternal {
      it.forEachIndexed { position, item ->
        it[position] = item.copy(state = InstructionItem.State.NONE)
      }
    }
  }

  /**
   * Update the "State" of the provided instruction to the given value.
   * This is a side-effect while executing a Program,
   * providing a UI hint about the success of each line.
   */
  private fun updateInstructionStatus(position: Int, state: InstructionItem.State) {
    if (position in 0 until instructions.value.size) {
      updateItemsInternal { items ->
        val instruction = items[position]
        instruction?.let { items[position] = instruction.copy(state = state) }
      }
    }
  }

  /**
   * Updates the list of Instructions held by the ViewModel
   * by means of the function passed in. Because this pattern
   * of mutating the item list is so common in the public API
   * of this ViewModel, it has been extracted to its own inline method.
   */
  private inline fun updateItemsInternal(function: (OrderedMap<InstructionItem>) -> Unit) {
    val items = instructions.value
    function.invoke(items)
    instructions.onNext(items)
  }
}
