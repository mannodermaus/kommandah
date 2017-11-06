package de.mannodermaus.kommandah.models

import org.threeten.bp.Duration

class OutputEvent(val instruction: Instruction)

data class ExecutionEnvironment(
    val speed: Duration = Duration.ofMillis(100)
)
