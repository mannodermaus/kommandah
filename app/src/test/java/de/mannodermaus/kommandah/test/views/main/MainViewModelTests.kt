package de.mannodermaus.kommandah.test.views.main

import de.mannodermaus.kommandah.managers.persistence.PersistenceManager
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.test.mocks.persistence.TestPersistenceManager
import de.mannodermaus.kommandah.test.utils.extensions.assertLatestValue
import de.mannodermaus.kommandah.test.utils.junit5.InjectExtension
import de.mannodermaus.kommandah.test.utils.junit5.Injected
import de.mannodermaus.kommandah.views.main.MainViewModel
import de.mannodermaus.kommandah.views.main.models.ConsoleEvent
import de.mannodermaus.kommandah.views.main.models.ExecutionState
import de.mannodermaus.kommandah.views.main.models.InstructionItem
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InjectExtension::class)
class MainViewModelTests {

  lateinit var viewModel: MainViewModel

  companion object {
    @BeforeAll
    @JvmStatic
    @Suppress("unused")
    fun beforeClass() {
      // Override default thread schedulers for Rx computations
      RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
      RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }
  }

  @BeforeEach
  fun beforeEach(@Injected viewModel: MainViewModel) {
    this.viewModel = viewModel
  }

  @Test
  @DisplayName("Add Instruction: Works as expected")
  fun addInstructionWorksAsExpected() {
    val observer = TestObserver<List<InstructionItem?>>()

    viewModel.instructions().subscribe(observer)
    observer.assertValueAt(0, emptyList())

    viewModel.addInstruction(Instruction.Stop)
    observer.assertValueAt(1, listOf(InstructionItem(Instruction.Stop)))
  }

  @Test
  @DisplayName("Swap Instructions: Works as expected")
  fun swapInstructionsWorksAsExpected() {
    viewModel.addInstruction(Instruction.Stop)
    viewModel.addInstruction(Instruction.Print)
    viewModel.addInstruction(Instruction.Push(1000))

    val observer = TestObserver<List<InstructionItem?>>()
    viewModel.instructions().subscribe(observer)

    observer.assertValueAt(0, listOf(
        InstructionItem(Instruction.Stop),
        InstructionItem(Instruction.Print),
        InstructionItem(Instruction.Push(1000))))

    viewModel.swapInstructions(2, 0)
    observer.assertValueAt(1, listOf(
        InstructionItem(Instruction.Push(1000)),
        InstructionItem(Instruction.Print),
        InstructionItem(Instruction.Stop)))
  }

  @Test
  @DisplayName("Update Instructions: Works as expected (no move to new index)")
  fun updateInstructionsWorksAsExpectedNoMove() {
    viewModel.addInstruction(Instruction.Stop)
    viewModel.addInstruction(Instruction.Print)
    viewModel.addInstruction(Instruction.Push(1000))

    val observer = TestObserver<List<InstructionItem?>>()
    viewModel.instructions().subscribe(observer)

    viewModel.updateInstruction(Instruction.Push(1234), 2, null)
    observer.assertValueAt(1, listOf(
        InstructionItem(Instruction.Stop),
        InstructionItem(Instruction.Print),
        InstructionItem(Instruction.Push(1234))))
  }

  @Test
  @DisplayName("Update Instructions: Works as expected (with move to new index)")
  fun updateInstructionsWorksAsExpectedWithMove() {
    viewModel.addInstruction(Instruction.Stop)
    viewModel.addInstruction(Instruction.Print)
    viewModel.addInstruction(Instruction.Push(1000))

    val observer = TestObserver<List<InstructionItem?>>()
    viewModel.instructions().subscribe(observer)

    viewModel.updateInstruction(Instruction.Push(1234), 2, 1)
    observer.assertValueAt(1, listOf(
        InstructionItem(Instruction.Stop),
        InstructionItem(Instruction.Push(1234)),
        InstructionItem(Instruction.Print)))
  }

  @Test
  @DisplayName("Remove Instruction: Works as expected")
  fun removeInstructionWorksAsExpected() {
    viewModel.addInstruction(Instruction.Push(1000))
    viewModel.addInstruction(Instruction.Print)
    viewModel.addInstruction(Instruction.Mult)
    viewModel.addInstruction(Instruction.Stop)

    val observer = TestObserver<List<InstructionItem?>>()
    viewModel.instructions().subscribe(observer)

    observer.assertValueAt(0, listOf(
        InstructionItem(Instruction.Push(1000)),
        InstructionItem(Instruction.Print),
        InstructionItem(Instruction.Mult),
        InstructionItem(Instruction.Stop)))

    viewModel.removeInstruction(2)
    observer.assertValueAt(1, listOf(
        InstructionItem(Instruction.Push(1000)),
        InstructionItem(Instruction.Print),
        InstructionItem(Instruction.Stop)))
  }

  @Test
  @DisplayName("Run Program: Produces correct Console Messages")
  fun runProgramProducesCorrectConsoleMessages() {
    // Small program that compiles
    viewModel.addInstruction(Instruction.Push(1000))
    viewModel.addInstruction(Instruction.Print)
    viewModel.addInstruction(Instruction.Stop)

    val observerSuccess = TestObserver<ConsoleEvent>()
    viewModel.consoleMessages().subscribe(observerSuccess)

    viewModel.runProgram()
    observerSuccess.assertValueSequence(listOf(
        ConsoleEvent.Clear,
        ConsoleEvent.Started(3),
        ConsoleEvent.Message(1, "1000"),
        ConsoleEvent.Finished
    ))
  }

  @Test
  @DisplayName("Save Program: Works as expected")
  fun saveProgramWorksAsExpected(@Injected persistenceManager: PersistenceManager) {
    val executionStateObserver = TestObserver<ExecutionState>()
    viewModel.executionState().subscribe(executionStateObserver)

    // Prepare and execute the Save operation
    viewModel.updateProgramTitle("My Program")
    viewModel.saveProgram()

    // Assert
    executionStateObserver.assertLatestValue { it.programTitle == "My Program" }
    assertThat((persistenceManager as TestPersistenceManager).infos.value).hasSize(1)
  }
}
