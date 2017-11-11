package de.mannodermaus.kommandah.test.views.main

import de.mannodermaus.kommandah.managers.runtime.Interpreter
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.test.managers.runtime.InstantInterpreter
import de.mannodermaus.kommandah.views.main.ConsoleEvent
import de.mannodermaus.kommandah.views.main.MainViewModel
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MainViewModelTests {

  lateinit var interpreter: Interpreter
  lateinit var viewModel: MainViewModel

  companion object {
    @BeforeAll
    @JvmStatic
    fun beforeClass() {
      // Override RxAndroid's default main thread scheduler
      // FIXME In a non-time-constrained environment, use DI to provide test schedulers
      RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }
  }

  @BeforeEach
  fun beforeEach() {
    interpreter = InstantInterpreter()
    viewModel = MainViewModel(interpreter)
  }

  @Test
  @DisplayName("Add Instruction: Works as expected")
  fun addInstructionWorksAsExpected() {
    val observer = TestObserver<List<Instruction>>()

    viewModel.instructions.subscribe(observer)
    observer.assertValueAt(0, emptyList())

    viewModel.addInstruction(Instruction.Stop)
    observer.assertValueAt(1, listOf(Instruction.Stop))
  }

  @Test
  @DisplayName("Swap Instructions: Works as expected")
  fun swapInstructionsWorksAsExpected() {
    viewModel.addInstruction(Instruction.Stop)
    viewModel.addInstruction(Instruction.Print)
    viewModel.addInstruction(Instruction.Push(1000))

    val observer = TestObserver<List<Instruction>>()
    viewModel.instructions.subscribe(observer)

    observer.assertValueAt(0, listOf(Instruction.Stop, Instruction.Print, Instruction.Push(1000)))

    viewModel.swapInstructions(2, 0)
    observer.assertValueAt(1, listOf(Instruction.Push(1000), Instruction.Print, Instruction.Stop))
  }

  @Test
  @DisplayName("Remove Instruction: Works as expected")
  fun removeInstructionWorksAsExpected() {
    viewModel.addInstruction(Instruction.Push(1000))
    viewModel.addInstruction(Instruction.Print)
    viewModel.addInstruction(Instruction.Mult)
    viewModel.addInstruction(Instruction.Stop)

    val observer = TestObserver<List<Instruction>>()
    viewModel.instructions.subscribe(observer)

    observer.assertValueAt(0, listOf(Instruction.Push(1000), Instruction.Print, Instruction.Mult, Instruction.Stop))

    viewModel.removeInstruction(2)
    observer.assertValueAt(1, listOf(Instruction.Push(1000), Instruction.Print, Instruction.Stop))
  }

  @Test
  @DisplayName("Run Program: Produces correct Console Messages")
  fun runProgramProducesCorrectConsoleMessages() {
    // Small program that compiles
    viewModel.addInstruction(Instruction.Push(1000))
    viewModel.addInstruction(Instruction.Print)
    viewModel.addInstruction(Instruction.Stop)

    val observerSuccess = TestObserver<ConsoleEvent>()
    viewModel.consoleMessages.subscribe(observerSuccess)

    viewModel.runProgram()
    observerSuccess.assertValueSequence(listOf(
        ConsoleEvent.Clear,
        ConsoleEvent.Started(3),
        ConsoleEvent.Message(1, "1000"),
        ConsoleEvent.Finished
    ))
  }
}
