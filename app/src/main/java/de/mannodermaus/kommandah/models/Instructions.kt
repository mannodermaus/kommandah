package de.mannodermaus.kommandah.models

import de.mannodermaus.kommandah.utils.extensions.bimapOf
import io.michaelrocks.bimap.BiMap

/* Types */

/**
 * Correlation between a set of instructions and their "memory address", i.e. index in a program.
 * easier insertions etc.
 */
typealias Instructions = Map<Int, Instruction?>

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
  val metadata = Instruction.metadataFromOperator(operator)
  override fun toString(): String = operator

  /* Concrete Types */

  /**
   * Jump to the instruction at index <i>address</i>
   */
  data class Call(val address: Int) : Instruction(operator = "CALL") {
    override fun toString(): String = "${super.toString()} $address"
  }

  /**
   * Pop two arguments from the stack, multiply them & push the result to the stack
   */
  object Mult : Instruction(operator = "MULT")

  /**
   * Pop one argument from the stack, print it out
   */
  object Print : Instruction(operator = "PRINT")

  /**
   * Push the given argument to the stack
   */
  data class Push(val argument: Int) : Instruction(operator = "PUSH") {
    override fun toString(): String = "${super.toString()} $argument"
  }

  /**
   * Pop one argument from the stack, jump to the instruction at that index
   */
  object Return : Instruction(operator = "RET")

  /**
   * Stop execution
   */
  object Stop : Instruction(operator = "STOP")

  companion object {
    /**
     * List of all Instruction operators
     */
    fun allOperators(): List<String> = instructionsMetadata.keys.toList().sorted()

    /**
     * Obtain the [InstructionMeta] object for the given Instruction operator.
     * Throws an [IllegalArgumentException] on unknown operators.
     */
    fun metadataFromOperator(operator: Operator): InstructionMeta =
        instructionsMetadata[operator] ?: throw IllegalArgumentException("Unknown Instruction operator: '$operator'")

    /**
     * Construct an [Instruction] from a String representation like "PUSH 12" or "PRINT".
     * Throws an [IllegalArgumentException] on unknown or invalid arguments.
     */
    fun fromString(value: String): Instruction {
      val split = value.split(" ")
      val operator = split[0]
      val metadata = instructionsMetadata[operator] ?: throw IllegalArgumentException("Can't deserialize Instruction from string '$value'")

      val params = metadata.parameterTypes
      val args = split.slice(1 until split.size)
      if (params.size != args.size) throw IllegalArgumentException("Argument count for '$value' doesn't match (expected ${params.size}, got ${args.size})")

      return metadata.createInstruction(*args
          .mapIndexed { index, arg -> params[index].convert(arg) }
          .toTypedArray())
    }
  }
}

/**
 * Descriptor of a parameter required by an [Instruction] to construct itself.
 */
sealed class InstructionParam<out T>(val name: String) {
  abstract fun convert(arg: Any): T

  class Int(name: String) : InstructionParam<kotlin.Int>(name) {
    override fun convert(arg: Any): kotlin.Int = arg.toString().toInt()
  }

  class Other(name: String) : InstructionParam<String>(name) {
    override fun convert(arg: Any): String = arg.toString()
  }
}

/**
 * Metadata about a specific [Instruction].
 * Used to inspect details about the Instruction in a way
 * that doesn't involve reflection, but still provides a generic
 * interface for serialization/deserialization or other means of creation.
 */
data class InstructionMeta(
    /** List of parameter types associated with the Instruction */
    val parameterTypes: List<InstructionParam<*>> = emptyList(),
    /** Factory for instantiating the Instruction with the given list of parameters */
    private val creator: (Array<*>) -> Instruction) {

  /** Operator value associated with the Instruction */
  val operator: Operator by lazy { instructionsMetadata.inverse[this]!! }
  /** Short-hand accessor for the presence of parameters in an Instruction */
  val hasParameters = parameterTypes.isNotEmpty()

  /**
   * Creates an Instruction using the given parameters.
   * Note: For Instructions without parameters, this
   * will always return the singleton instance of that Instruction.
   */
  fun createInstruction(vararg args: Any? = emptyArray()) = creator(args)
}

/**
 * Mapping of all available Instruction Metadata classes to their operator.
 */
private val instructionsMetadata: BiMap<String, InstructionMeta> =
    bimapOf(
        "CALL" to InstructionMeta(
            parameterTypes = listOf(InstructionParam.Int("address")),
            creator = { Instruction.Call(it[0] as Int) }),

        "MULT" to InstructionMeta { Instruction.Mult },
        "PRINT" to InstructionMeta { Instruction.Print },

        "PUSH" to InstructionMeta(
            parameterTypes = listOf(InstructionParam.Int("argument")),
            creator = { Instruction.Push(it[0] as Int) }),

        "RET" to InstructionMeta { Instruction.Return },
        "STOP" to InstructionMeta { Instruction.Stop }
    )
