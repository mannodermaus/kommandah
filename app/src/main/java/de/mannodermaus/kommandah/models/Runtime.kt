package de.mannodermaus.kommandah.models

import org.threeten.bp.Duration

sealed class ProgramOutput {

  /**
   * Execution started.
   */
  data class Started(val numLines: Int): ProgramOutput()

  /**
   * A single instruction was executed,
   * potentially generating a log message as a side-effect.
   */
  data class Step(val instruction: Instruction, val line: Int, val message: String? = null) : ProgramOutput()

  /**
   * An error occurred at the given instruction,
   * and execution was halted because of it.
   */
  data class Error(val cause: ProgramException, val line: Int, val instruction: Instruction? = null) : ProgramOutput()

  /**
   * Execution finished.
   */
  object Completed : ProgramOutput()
}

/**
 * Representation of a Program's "return status",
 * i.e. if execution was successful or not.
 */
sealed class ExitCode {
  /**
   * Not returned yet (either "not started" or "still running")
   */
  object None : ExitCode()

  /**
   * Successful execution
   */
  object Success : ExitCode()

  /**
   * Failed with error
   */
  data class Error(val cause: ProgramException) : ExitCode()
}

/**
 * Configurable parameters for the execution of a Program using an Interpreter.
 */
data class ExecutionEnvironment(
    /**
     * The speed at which executions should be processed.
     * TODO I'm envisioning a slider in the app UI that dynamically changes this property
     */
    val speed: Duration = Duration.ofMillis(500)
)
