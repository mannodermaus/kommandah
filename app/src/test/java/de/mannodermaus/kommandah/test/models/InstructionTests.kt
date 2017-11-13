@file:Suppress("unused")

package de.mannodermaus.kommandah.test.models

import de.mannodermaus.kommandah.models.Instruction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * These unit tests are verifying the behaviour of
 * serialization and deserialization of [Instruction] values.
 */
open class InstructionTests {

  companion object {
    @JvmStatic
    fun allInstructions(): Stream<Arguments> =
        Stream.of(
            Arguments.of(Instruction.Call(100), "CALL 100"),
            Arguments.of(Instruction.Mult, "MULT"),
            Arguments.of(Instruction.Return, "RET"),
            Arguments.of(Instruction.Stop, "STOP"),
            Arguments.of(Instruction.Print, "PRINT"),
            Arguments.of(Instruction.Push(1337), "PUSH 1337"))
  }

  @Nested
  @DisplayName("Serialization")
  class SerializationTests : InstructionTests() {
    @ParameterizedTest
    @MethodSource("allInstructions")
    @DisplayName("Serialization works as expected")
    fun serializationWorksAsExpected(instruction: Instruction, serialized: String) {
      assertThat(instruction.toString()).isEqualTo(serialized)
    }
  }

  @Nested
  @DisplayName("Deserialization")
  class DeserializationTests : InstructionTests() {

    companion object {
      @JvmStatic
      fun instructionsWithIllegalParamCount(): Stream<Arguments> =
          Stream.of(
              // "Operator $0 expected $1 arguments, but got $2 instead"
              Arguments.of("PUSH", 1, 0),
              Arguments.of("PRINT 55", 0, 1),
              Arguments.of("CALL 555 1234 9090", 1, 3))
    }

    @ParameterizedTest
    @MethodSource("allInstructions")
    @DisplayName("Works as expected")
    fun worksAsExpected(instruction: Instruction, serialized: String) {
      assertThat(Instruction.fromString(serialized)).isEqualTo(instruction)
    }

    @Test
    @DisplayName("Throws Exception on unknown Instruction")
    fun throwsOnUnknownInstruction() {
      assertThrows(IllegalArgumentException::class.java) {
        Instruction.fromString("WTF 1337")
      }
    }

    @ParameterizedTest
    @MethodSource("instructionsWithIllegalParamCount")
    @DisplayName("Throws Exception on unexpected parameter count")
    fun throwsOnUnexpectedParamCount(serialized: String, expected: Int, got: Int) {
      val error = assertThrows(IllegalArgumentException::class.java) {
        Instruction.fromString(serialized)
      }
      assertThat(error.message).contains("expected $expected, got $got")
    }
  }
}
