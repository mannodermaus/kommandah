package de.mannodermaus.kommandah.models

/* Types */

/**
 * Correlation between a set of instructions and their "memory address", i.e. index in a program.
 *
 * TODO This typealias will eventually be replaced by a List-like interface which allows
 * easier insertions etc.
 */
typealias InstructionStack = Map<Long, Instruction>

/**
 * The different instructions understood by the Interpreter.
 */
sealed class Instruction(protected val operator: String)

/**
 * Pop two arguments from the stack, multiply them & push the result to the stack
 */
class Mult : Instruction(operator = "MULT")

/**
 * Jump to the instruction at index <i>address</i>
 */
class Call(val address: Long) : Instruction(operator = "CALL")

/**
 * Pop one argument from the stack, jump to the instruction at that index
 */
class Return : Instruction(operator = "RET")

/**
 * Stop execution
 */
class Stop : Instruction(operator = "STOP")

/**
 * Pop one argument from the stack, print it out
 */
class Print : Instruction(operator = "PRINT")

/**
 * Push the given argument to the stack
 */
class Push(val argument: Long) : Instruction(operator = "PUSH")

/* Extensions */

/**
 * Compile a set of Instructions into a Program,
 * which can then be executed by an Interpreter.
 */
fun InstructionStack.compile(): Program = Program(this)
