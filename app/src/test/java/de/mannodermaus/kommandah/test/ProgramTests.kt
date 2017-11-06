package de.mannodermaus.kommandah.test

import de.mannodermaus.kommandah.models.Call
import de.mannodermaus.kommandah.models.Mult
import de.mannodermaus.kommandah.models.OutputEvent
import de.mannodermaus.kommandah.models.Print
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.Push
import de.mannodermaus.kommandah.models.Return
import de.mannodermaus.kommandah.models.Stop
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
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
    assertEquals(Push(1009), program[0]!!.instruction)
    assertEquals(Print, program[1]!!.instruction)
    assertEquals(Stop, program[2]!!.instruction)
    assertNull(program[3])
    assertEquals(Push(10), program[4]!!.instruction)
    assertEquals(Return, program[5]!!.instruction)
  }

  @Nested
  @DisplayName("Instruction: MULT")
  class MultExecutionTests {

    companion object {
      val MULT = mapOf(0 to Mult, 1 to Stop)
      val COMPLETED = OutputEvent.Completed
    }

    /* Helpers */

    private fun createProgramWith(stack: Stack<Int>) =
        Program(instructions = MULT, stack = stack)

    private fun result(number: Int): OutputEvent =
        OutputEvent.Calc(
            instruction = Mult,
            result = number)

    /* Tests */

    @Test
    @DisplayName("Works as expected with enough arguments on the Stack")
    fun multWorksWithEnoughArgumentsOnTheStack() {
      val program = createProgramWith(stackOf(5, 6))
      val results = program.run()
      assertThat(results).containsOnly(result(30), COMPLETED)
    }

    @Test
    @DisplayName("Throws when stack has only one element")
    fun multThrowsWithOnlyOneElementOnTheStack() {
      val program = createProgramWith(stackOf(1))
      val results = program.run()
      assertThat(results)
          .allMatch { it is OutputEvent.Error && it.cause is EmptyStackException }
    }

    @Test
    @DisplayName("Throws when stack is empty")
    fun multThrowsWhenStackIsEmpty() {
      val program = createProgramWith(stackOf())
      val results = program.run()
      assertThat(results)
          .allMatch { it is OutputEvent.Error && it.cause is EmptyStackException }
    }
  }

  @Nested
  @DisplayName("Instruction: CALL")
  class CallExecutionTests {

    @Test
    @DisplayName("Works as expected")
    fun worksAsExpected() {
      val program = Program(mapOf(
          0 to Call(5),
          3 to Call(20),
          5 to Call(3),
          20 to Stop
      ))
      val results = program.run()
      assertThat(results
          .filter { it is OutputEvent.Void })
          .extracting("instruction")
          .extracting("address")
          .containsExactly(5, 3, 20)
    }
  }
}
