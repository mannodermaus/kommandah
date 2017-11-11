package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModel
import de.mannodermaus.kommandah.managers.runtime.Interpreter
import de.mannodermaus.kommandah.models.ExecutionEnvironment
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramOutput
import de.mannodermaus.kommandah.utils.extensions.async
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

class MainViewModel
@Inject constructor(private val interpreter: Interpreter) : ViewModel() {

  /**
   * Stream of events related to the Instructions that make up the current Program
   */
  val instructions: BehaviorSubject<List<Instruction>> =
      BehaviorSubject.createDefault(emptyList())

  /**
   * Stream of events related to the current execution status of the current Program
   */
  val executionStatus: BehaviorSubject<ExecutionStatus> =
      BehaviorSubject.createDefault(ExecutionStatus.PAUSED)

  /**
   * Stream of events related to handling console messages during execution
   */
  val consoleMessages: BehaviorSubject<ConsoleEvent> =
      BehaviorSubject.create()

  private val subscriptions: CompositeDisposable = CompositeDisposable()

  /* Lifecycle */

  override fun onCleared() {
    super.onCleared()
    subscriptions.clear()
  }

  /* Interactions */

  fun addInstruction(instruction: Instruction) {
    val items = instructions.value.toMutableList()
    items += instruction
    instructions.onNext(items)
  }

  fun swapInstructions(fromPosition: Int, toPosition: Int) {
    // Swap the two items in question, then also swap their indices
    val items = instructions.value.toMutableList()
    Collections.swap(items, fromPosition, toPosition)
    instructions.onNext(items)
  }

  fun removeInstruction(position: Int) {
    val items = instructions.value.toMutableList()
    items.removeAt(position)
    instructions.onNext(items)
  }

  fun runProgram() {
    // Compile the instructions into a Program
    val program = Program(instructions.value
        .withIndex()
        .associate { it.index to it.value })

    // Interpret the program, passing through events as side-effects
    subscriptions += interpreter.execute(program, ExecutionEnvironment())
        .async()
        .doOnSubscribe {
          // Update the execution status
          executionStatus.onNext(ExecutionStatus.RUNNING)
          consoleMessages.onNext(ConsoleEvent.Clear)
        }
        .doOnNext {
          // Propagate log messages through the Console stream,
          // converting each into the View Realm's data types
          when (it) {
            is ProgramOutput.Started -> consoleMessages.onNext(ConsoleEvent.Started(it.numLines))
            is ProgramOutput.Log -> consoleMessages.onNext(ConsoleEvent.Message(it.line, it.message))
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
}
