package de.mannodermaus.kommandah.test.managers.persistence

import de.mannodermaus.kommandah.managers.persistence.impl.deserializeProgram
import de.mannodermaus.kommandah.managers.persistence.impl.serializeProgram
import de.mannodermaus.kommandah.models.Instruction
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.test.mocks.persistence.Java8Base64Factory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RoomPersistenceTests {

  @Test
  fun roundTrip() {
    val program = Program(mapOf(
        0 to Instruction.Push(12),
        1 to Instruction.Print,
        0 to Instruction.Push(12)))

    val factory = Java8Base64Factory()
    val serialized = serializeProgram(program, factory)
    val reconstructed = deserializeProgram(serialized, factory)
    assertThat(program).isEqualTo(reconstructed)
  }
}
