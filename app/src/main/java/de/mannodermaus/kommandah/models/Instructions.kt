package de.mannodermaus.kommandah.models

/* Types */

/**
 * Correlation between a set of instructions and their "memory address", i.e. index in a program.
 *
 * TODO This typealias will eventually be replaced by a List-like interface which allows
 * easier insertions etc.
 */
typealias Instructions = Map<Int, Instruction>

/**
 * The different instructions understood by the Interpreter.
 */
sealed class Instruction(protected val operator: String)

/**
 * Pop two arguments from the stack, multiply them & push the result to the stack
 */
object Mult : Instruction(operator = "MULT")

/**
 * Jump to the instruction at index <i>address</i>
 */
data class Call(val address: Int) : Instruction(operator = "CALL")

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
data class Push(val argument: Int) : Instruction(operator = "PUSH")

/* Extensions */

/**
 * Compile a set of Instructions into a Program,
 * which can then be executed by an Interpreter.
 */
fun Instructions.compile(): Program = Program(this)
