package de.mannodermaus.kommandah.models

import de.mannodermaus.kommandah.utils.extensions.bimapOf

/* Types */

/**
 * Correlation between a set of instructions and their "memory address", i.e. index in a program.
 *
 * TODO This typealias will eventually be replaced by a List-like interface which allows
 * easier insertions etc.
 */
typealias Instructions = Map<Int, Instruction>

typealias Operator = String

/**
 * The different instructions understood by the Interpreter.
 *
 * NOTE: When a new Instruction is added, its metadata signature
 * needs to be passed to the [instructionsMetadata] Map defined below.
 * This is required to remove the need for reflective access to the
 * available instructions at runtime
 */
sealed class Instruction(protected val operator: String) {

  /* Functions */

  fun metadata(): InstructionMeta = Instruction.metadataFromOperator(operator)
  open fun describe(): String = operator
  override fun toString(): String = javaClass.simpleName

  /* Concrete Types */

  /**
   * Pop two arguments from the stack, multiply them & push the result to the stack
   */
  object Mult : Instruction(operator = "MULT")

  /**
   * Jump to the instruction at index <i>address</i>
   */
  data class Call(val address: Int) : Instruction(operator = "CALL") {
    override fun describe(): String = "${super.describe()} $address"
  }

  /**
   * Pop one argument from the stack, jump to the instruction at that index
   */
  object Return : Instruction(operator = "RET")

  /**
   * Stop execution
   */
  object Stop : Instruction(operator = "STOP")

  /**
   * Pop one argument from the stack, print it out
   */
  object Print : Instruction(operator = "PRINT")

  /**
   * Push the given argument to the stack
   */
  data class Push(val argument: Int) : Instruction(operator = "PUSH") {
    override fun describe(): String = "${super.describe()} $argument"
  }

  companion object {
    fun allOperators(): List<String> = instructionsMetadata.keys.toList().sorted()
    fun metadataFromOperator(operator: Operator): InstructionMeta =
        instructionsMetadata[operator] ?: throw IllegalArgumentException("Unknown Instruction operator: '$operator'")
  }
}

sealed class InstructionParam(val name: String) {
  class Int(name: String) : InstructionParam(name)
  class Other(name: String) : InstructionParam(name)
}

/**
 * Metadata about a specific [Instruction].
 */
data class InstructionMeta(
    val parameters: List<InstructionParam> = emptyList(),
    private val creator: (Array<*>) -> Instruction) {

  val operator: Operator by lazy { instructionsMetadata.inverse[this]!! }
  val hasParameters = parameters.isNotEmpty()

  /**
   * Creates an Instruction using the given parameters.
   * Note: For Instructions without parameters, this
   * will always return the singleton instance of that Instruction.
   */
  fun createInstruction(vararg args: Any? = emptyArray()) = creator.invoke(args)
}

private val instructionsMetadata = bimapOf(
    "CALL" to InstructionMeta(listOf(InstructionParam.Int("address"))) {
      Instruction.Call(it[0] as Int)
    },
    "MULT" to InstructionMeta { Instruction.Mult },
    "PRINT" to InstructionMeta { Instruction.Print },
    "PUSH" to InstructionMeta(listOf(InstructionParam.Int("value"))) {
      Instruction.Push(it[0] as Int)
    },
    "RET" to InstructionMeta { Instruction.Return },
    "STOP" to InstructionMeta { Instruction.Stop }
)
