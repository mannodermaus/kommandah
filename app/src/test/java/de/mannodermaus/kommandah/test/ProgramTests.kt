package de.mannodermaus.kommandah.test

import de.mannodermaus.kommandah.models.AlreadyExecuted
import de.mannodermaus.kommandah.models.Call
import de.mannodermaus.kommandah.models.Mult
import de.mannodermaus.kommandah.models.OutputEvent
import de.mannodermaus.kommandah.models.Print
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.Push
import de.mannodermaus.kommandah.models.Return
import de.mannodermaus.kommandah.models.Stop
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

class ProgramTests {

  @Test
  @DisplayName("Keeps Instruction order when converting to Lines")
  fun keepsInstructionOrderWhenConvertingToLines() {
    val instructions = mapOf(
        0 to Push(1009),
        1 to Print,
        2 to Stop,

        4 to Push(10),
        5 to Return
    )

    val program = Program(instructions)
    assertEquals(Push(1009), program.instructionAt(0))
    assertEquals(Print, program.instructionAt(1))
    assertEquals(Stop, program.instructionAt(2))
    assertNull(program.instructionAt(3))
    assertEquals(Push(10), program.instructionAt(4))
    assertEquals(Return, program.instructionAt(5))
  }

  @Test
  @DisplayName("Can't execute more than once")
  fun cantExecuteMoreThanOnce() {
    val program = Program(mapOf(
        0 to Push(1009),
        1 to Print,
        2 to Stop
    ))

    program.runBlocking()
    program.assertExecutionSuccessful()

    // Another attempt at executing
    Assertions.assertThrows(AlreadyExecuted::class.java) { program.runBlocking() }
  }

  @Test
  @DisplayName("Can be executed again after copy")
  fun canBeExecutedAgainAfterCopy() {
    val program = Program(mapOf(
        0 to Push(1009),
        1 to Print,
        2 to Stop
    ))

    program.runBlocking()
    program.assertExecutionSuccessful()

    val copy = program.copy()
    copy.runBlocking()
    copy.assertExecutionSuccessful()
  }
}

@DisplayName("Instruction: MULT")
class MultInstructionTests {

  /* Helpers */

  private fun createProgramWith(stack: Stack<Int>) =
      Program(instructions = mapOf(0 to Mult, 1 to Stop), stack = stack)

  /* Tests */

  @Test
  @DisplayName("Works as expected with enough arguments on the Stack")
  fun multWorksWithEnoughArgumentsOnTheStack() {
    val program = createProgramWith(stackOf(5, 6))

    val results = program.runBlocking()

    program.assertExecutionSuccessful()
    assertThat(results)
        .containsOnly(
            OutputEvent.Calc(instruction = Mult, result = 30),
            OutputEvent.Completed)
  }

  @Test
  @DisplayName("Throws when stack has only one element")
  fun multThrowsWithOnlyOneElementOnTheStack() {
    val program = createProgramWith(stackOf(1))

    program.runBlocking()

    program.assertExecutionFailedWith<EmptyStackException>()
  }

  @Test
  @DisplayName("Throws when stack is empty")
  fun multThrowsWhenStackIsEmpty() {
    val program = createProgramWith(stackOf())

    program.runBlocking()

    program.assertExecutionFailedWith<EmptyStackException>()
  }
}

@DisplayName("Instruction: CALL")
class CallInstructionTests {

  @Test
  @DisplayName("Works as expected")
  fun worksAsExpected() {
    // Jump around to different address values in order
    val program = Program(mapOf(
        0 to Call(5),
        3 to Call(20),
        5 to Call(3),
        20 to Stop
    ))

    val results = program.runBlocking()

    program.assertExecutionSuccessful()
    assertThat(results
        .filter { it is OutputEvent.Void })
        .extracting("instruction")
        .extracting("address")
        .containsExactly(5, 3, 20)
  }
}

@DisplayName("Instruction: RET")
class ReturnInstructionTests {

  @Test
  @DisplayName("Works as expected with enough arguments on the stack")
  fun worksWithEnoughArgumentsOnTheStack() {
    // Expect the RET statement to move to "address 10", then complete
    val stack = stackOf(10)
    val program = Program(
        instructions = mapOf(
            0 to Return,
            10 to Stop
        ),
        stack = stack)

    val results = program.runBlocking()

    program.assertExecutionSuccessful()
    assertThat(results)
        .containsExactly(
            OutputEvent.Void(Return),
            OutputEvent.Completed)
  }

  @Test
  @DisplayName("Throws on empty stack")
  fun throwsOnEmptyStack() {
    val program = Program(
        instructions = mapOf(
            0 to Return,
            10 to Stop
        ))

    program.runBlocking()

    program.assertExecutionFailedWith<EmptyStackException>()
  }
}

@DisplayName("Instruction: STOP")
class StopInstructionTests {

  @Test
  @DisplayName("Works as expected")
  fun worksAsExpected() {
    // Don't move onto index #1, which is undefined
    val program = Program(mapOf(0 to Stop))

    val results = program.runBlocking()

    program.assertExecutionSuccessful()
    assertThat(results).containsOnly(OutputEvent.Completed)
  }
}

@DisplayName("Instruction: PRINT")
class PrintInstructionTests {

  @Test
  @DisplayName("Works as expected with enough arguments on the stack")
  fun worksAsExpectedWithEnoughArgumentsOnTheStack() {
    val stack = stackOf(1337)
    val program = Program(
        instructions = mapOf(0 to Print, 1 to Stop),
        stack = stack)

    val results = program.runBlocking()

    program.assertExecutionSuccessful()
    assertThat(results).hasSize(2)
        .element(0)
        .isInstanceOf(OutputEvent.Log::class.java)
        .extracting("message")
        .hasOnlyOneElementSatisfying { it == "1337" }
  }

  @Test
  @DisplayName("Throws on empty stack")
  fun throwsOnEmptyStack() {
    val program = Program(
        instructions = mapOf(0 to Print, 1 to Stop),
        stack = stackOf())

    program.runBlocking()

    program.assertExecutionFailedWith<EmptyStackException>()
  }
}

@DisplayName("Instruction: PUSH")
class PushInstructionTests {

  @Test
  @DisplayName("Works as expected")
  fun worksAsExpected() {
    val stack = stackOf<Int>()
    val program = Program(
        instructions = mapOf(
            0 to Push(1234),
            1 to Push(3550),
            2 to Stop),
        stack = stack)

    program.runBlocking()

    program.assertExecutionSuccessful()
    assertThat(stack).containsExactly(1234, 3550)
  }
}
