package de.mannodermaus.kommandah.models

/**
 * Base class for Failures within a Program's execution
 */
sealed class ProgramException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
  class IllegalStackAccess(val line: Int) : ProgramException("Illegal Stack access at L$line")
  class IllegalInstructionAccess(val line: Int) : ProgramException("Illegal Instruction access at L$line")
  class AlreadyExecuted : ProgramException("Program can't be executed more than once; use #copy() beforehand")
  class Unknown(cause: Throwable) : ProgramException("Unknown error: ${cause.javaClass}", cause)
  // TODO InfiniteLoop etc.
}

