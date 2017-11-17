package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModel
import de.mannodermaus.kommandah.managers.persistence.PersistenceManager
import de.mannodermaus.kommandah.managers.runtime.Interpreter
import de.mannodermaus.kommandah.models.ExecutionEnvironment
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.models.OrderedMap
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramInfo
import de.mannodermaus.kommandah.models.ProgramOutput
import de.mannodermaus.kommandah.utils.extensions.async
import de.mannodermaus.kommandah.views.main.models.ConsoleEvent
import de.mannodermaus.kommandah.views.main.models.ExecutionState
import de.mannodermaus.kommandah.views.main.models.InstructionItem
import de.mannodermaus.kommandah.views.main.models.ProgramEvent
import de.mannodermaus.kommandah.views.main.models.ProgramState
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val interpreter: Interpreter,
    private val persistence: PersistenceManager
) : ViewModel() {

  private val programStateChanges: BehaviorSubject<ProgramState> =
      BehaviorSubject.createDefault(ProgramState())
  private val programEventsStream: PublishSubject<ProgramEvent> =
      PublishSubject.create()
  private val consoleMessagesStream: BehaviorSubject<ConsoleEvent> =
      BehaviorSubject.create()
  private val subscriptions: CompositeDisposable = CompositeDisposable()

  /* Lifecycle */

  override fun onCleared() {
    super.onCleared()
    subscriptions.clear()
  }

  /* Streams; doubled from properties to hide BehaviorSubjects from consumers */

  /**
   * Stream of changes to the List of Instructions that make up the current Program.
   * Note that the List contains null values on "absent"/"empty" items.
   */
  fun instructions(): Observable<List<InstructionItem?>> =
      programStateChanges.map { it.instructions.nullPaddedValues }
          .distinctUntilChanged()
          .async()

  /**
   * Stream of changes to the Program's execution status, either "Executing" or "Paused".
   */
  fun executionState(): Observable<ExecutionState> =
      programStateChanges.map { ExecutionState(running = it.running, programTitle = it.title) }
          .distinctUntilChanged()
          .async()

  /**
   * Stream of events related to loading & saving Program information
   */
  fun programEvents(): Observable<ProgramEvent> = programEventsStream.async()

  /**
   * Stream of messages to print to a Console.
   */
  fun consoleMessages(): Observable<ConsoleEvent> = consoleMessagesStream.async()

  /* Interactions */

  /**
   * Obtains the list of saved Programs asynchronously.
   */
  fun listRecentPrograms(count: Int) = persistence.listRecentPrograms(count).async()

  /**
   * Starts a new, blank Program.
   */
  fun newProgram() {
    updateProgramState { ProgramState() }
    notifyProgramEvent(ProgramEvent.New)
  }

  /**
   * Loads the given Program into the ViewModel.
   */
  fun loadProgram(info: ProgramInfo) {
    subscriptions += info.load()
        .async()
        .subscribe { program ->
          // Convert the Program's instructions into
          // the mutable structure exposed by the screen.
          val mappedInstructions = OrderedMap(program.instructions
              .filter { it.value != null }
              .map { it.key to InstructionItem(it.value!!) }
              .associate { it })

          updateProgramState {
            it.copy(
                savedId = info.id,
                title = info.title,
                instructions = mappedInstructions)
          }
          notifyProgramEvent(ProgramEvent.Loaded(info.title))
        }
  }

  /**
   * Updates the current Program's title with the given text.
   */
  fun updateProgramTitle(title: String) {
    updateProgramState { it.copy(title = title) }
  }

  /**
   * Stores the current Program asynchronously, assigning the given title to it.
   */
  fun saveProgram() {
    subscriptions += persistence.saveProgram(
        program = compileInstructions(),
        id = currentProgramState.savedId,
        title = currentProgramState.title)
        .async()
        .subscribe { result ->
          updateProgramState {
            // Store the persistence identifiers locally as well
            it.copy(
                savedId = result.id,
                title = result.title)
          }
          notifyProgramEvent(ProgramEvent.Saved(result.title))
        }
  }

  /**
   * Using the current List of Instructions, execute the Program
   * and notify subscribers of the other stream-based functions along the way.
   */
  fun runProgram() {
    val program = compileInstructions()

    // Interpret the program, passing through events as side-effects
    subscriptions += interpreter.execute(program, ExecutionEnvironment())
        .async()
        .doOnSubscribe {
          // Update the execution status
          clearInstructionState()
          updateProgramState { it.copy(running = true) }
          consoleMessagesStream.onNext(ConsoleEvent.Clear)
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
            is ProgramOutput.Started -> consoleMessagesStream.onNext(ConsoleEvent.Started(it.numLines))
            is ProgramOutput.Step -> it.message?.let { msg -> consoleMessagesStream.onNext(ConsoleEvent.Message(it.line, msg)) }
            is ProgramOutput.Error -> consoleMessagesStream.onNext(ConsoleEvent.Error(it.cause, it.instruction))
            is ProgramOutput.Completed -> consoleMessagesStream.onNext(ConsoleEvent.Finished)
          }
        }
        .doOnTerminate {
          // Reset the execution status
          updateProgramState { it.copy(running = false) }
        }
        .subscribe()
  }

  /**
   * Appends the given instruction to the end of the Program,
   * then fires a notification to subscribers of the data.
   */
  fun addInstruction(instruction: Instruction) {
    updateInstructions { it += InstructionItem(instruction) }
  }

  /**
   * Update the given instruction at the provided index, if any,
   * then fires a notification to subscribers of the data.
   * If no positional index is given, the instruction is appended to the end.
   */
  fun updateInstruction(instruction: Instruction, position: Int, moveToPosition: Int?) {
    updateInstructions {
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
    updateInstructions { it.swap(fromPosition, toPosition) }
  }

  /**
   * Removes the instruction at the given position,
   * then fires a notification to subscribers of the data.
   */
  fun removeInstruction(position: Int) {
    updateInstructions { it.remove(position) }
  }

  /**
   * Checks whether or not there is an Instruction placed at the given position.
   */
  fun hasInstructionAt(position: Int): Boolean = currentProgramState.instructions.containsKey(position)

  /* Private */

  /**
   * Compiles the current list of Instructions into a Program, and returns that.
   */
  private fun compileInstructions() =
      Program(currentProgramState.instructions.nullPaddedValues
          .withIndex()
          .associate { it.index to it.value?.instruction })

  /**
   * Reset all items' "State" to NONE.
   * This is invoked upon executing a Program to clear all UI hints
   * related to success or error for each instruction.
   */
  private fun clearInstructionState() {
    updateInstructions {
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
    if (position in 0 until currentProgramState.instructions.size) {
      updateInstructions { items ->
        val instruction = items[position]
        instruction?.let { items[position] = instruction.copy(state = state) }
      }
    }
  }

  /**
   * Updates the current Execution State by means of the function passed in.
   */
  private inline fun updateProgramState(function: (ProgramState) -> ProgramState) {
    val currentState = programStateChanges.value
    val newState = function.invoke(currentState)
    programStateChanges.onNext(newState)
  }

  private fun notifyProgramEvent(event: ProgramEvent) {
    programEventsStream.onNext(event)
  }

  /**
   * Updates the list of Instructions held by the ViewModel
   * by means of the function passed in. Because this pattern
   * of mutating the item list is so common in the public API
   * of this ViewModel, it has been extracted to its own inline method.
   */
  private inline fun updateInstructions(function: (OrderedMap<InstructionItem>) -> Unit) {
    val currentInstructions = currentProgramState.instructions
    function.invoke(currentInstructions)
    programStateChanges.onNext(currentProgramState.copy(instructions = currentInstructions))
  }

  private val currentProgramState
    get() = programStateChanges.value
}
