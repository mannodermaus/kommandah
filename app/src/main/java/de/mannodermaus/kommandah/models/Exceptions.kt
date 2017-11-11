package de.mannodermaus.kommandah.models

/**
 * Base class for Failures within a Program's execution
 */
sealed class ProgramException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
  /**
   * A value is requested from the "variable stack", but none is available.
   * Maps pretty closely to java.util.EmptyStackException in terms of how it occurs.
   */
  class IllegalStackAccess(val line: Int)
    : ProgramException("Illegal Stack access on L$line")

  /**
   * An instruction is accessed at an illegal pointer index.
   * Maps pretty closely to kotlin.NoSuchElementException in terms of how it occurs.
   * @param line Line in the Progam where the error occurs
   * @param address Address that can't be accessed
   */
  class IllegalInstructionAccess(val line: Int, val address: Int)
    : ProgramException("Illegal Access on L$line: No instruction at address $address")

  /**
   * Special case of an illegal access,
   * caused by a missing Stop instruction at the end of the Program.
   */
  class MissingStopInstruction(val line: Int)
    : ProgramException("Illegal Access on L$line: Did you forget to put a Stop instruction at the end?")

  /**
   *
   */
  class AlreadyExecuted : ProgramException("Program can't be executed more than once; use #copy() beforehand")

  class Unknown(cause: Throwable) : ProgramException("Unknown error: ${cause.javaClass}", cause)
  // TODO InfiniteLoop etc.
}

/**
 * Thrown during Program execution when an instruction is referenced
 * at a non-existing address.
 *
 * Will be mapped to a [ProgramException] before propagating to the user
 */
class NoSuchInstructionError(val address: Int) : Error()
