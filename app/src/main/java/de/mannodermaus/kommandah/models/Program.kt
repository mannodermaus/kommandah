package de.mannodermaus.kommandah.models

import android.support.annotation.CheckResult
import java.util.*

/**
 * Representation of a Program to be executed by an Interpreter.
 *
 * This class exposes its execution capabilities as a Sequence
 * which allows it to be usable in a loop,
 * executing each instruction one by one on-demand:
 *
 * <code><pre>
 *  val program = Program(instructions)
 *
 *  for (output in program.run()) {
 *    when (output) {
 *      is OutputEvent.Log -> ...
 *      is OutputEvent.Error -> ...
 *      else -> ...
 *    }
 *  }
 * </pre></code>
 *
 * Additionally, the Program can be executed "all at once",
 * in a synchronous fashion,  using [runBlocking]:
 *
 * <code><pre>
 *  val program = Program(instructions)
 *  val outputs = program.runBlocking()
 * </pre></code>
 */
data class Program(
    private val instructions: Instructions,
    // Storage for "virtual variables"
    private val stack: Stack<Int> = Stack()) {

  /**
   * The result of the Program's execution, assigned only after it was fully executed.
   */
  var exitCode: ExitCode = ExitCode.None
    private set(value) {}

  /**
   * Internal mapping of Instructions to a type that knows how to execute them.
   */
  private val lines: Map<Int, Line> = instructions.entries
      .associate { it.key to Line(it.value) }

  // Program Counter
  private var pc: Int = 0

  fun instructionAt(index: Int): Instruction? = instructions[index]

  /**
   * Returns the execution of the Program as a lazy Sequence.
   * Note: For a quick, synchronous way of executing the Program, use [runBlocking].
   */
  @CheckResult
  fun run(): Sequence<OutputEvent> {
    if (exitCode != ExitCode.None) {
      // Already executed; requires a copy() to run again
      throw AlreadyExecuted()
    }

    return generateSequence {
      if (exitCode != ExitCode.None) {
        // There is a distinct exit code; return null to finish sequence
        null

      } else {
        val line = lines[pc] ?: throw SegmentationFault(pc)
        if (line.instruction is Stop) {
          // Toggle successful execution
          exitCode = ExitCode.Success
        }
        pc++

        val result = line.execute()
        if (result is OutputEvent.Error) {
          // Toggle erroneous execution
          exitCode = ExitCode.Error(result.cause)
        }
        result
      }
    }
  }

  /**
   * Synchronously runs the Program to completion, or until an Error is generated.
   * After calling this, the [exitCode] is guaranteed to be either
   * [ExitCode.Success] or [ExitCode.Error].
   */
  fun runBlocking(): List<OutputEvent> = run().toList()

  /**
   * Representation of a single line inside a program,
   * backed by a low-level Instruction.
   */
  private inner class Line(val instruction: Instruction) {
    fun execute(): OutputEvent {
      try {
        when (instruction) {
          is Mult -> {
            // "Pop two values from the stack, multiply them, push the result back"
            val value1 = stack.pop()
            val value2 = stack.pop()
            val result = value1 * value2
            stack.push(result)
            return OutputEvent.Calc(instruction, result)
          }

          is Call ->
            // "Jump to the instruction at the given value"
            pc = instruction.address

          is Return ->
            // "Pop one argument from the stack, jump to the instruction at that address"
            pc = stack.pop()

          is Stop ->
            // "Stop the execution"
            return OutputEvent.Completed

          is Print -> {
            // "Pop one argument from the stack, print it out"
            val argument = stack.pop()
            return OutputEvent.Log(instruction, "$argument")
          }

          is Push ->
            // "Push the given argument to the stack"
            stack.push(instruction.argument)
        }

        // Default result value
        return OutputEvent.Void(instruction)

      } catch (cause: Throwable) {
        // Assume construction error by the user
        return OutputEvent.Error(instruction, cause)
      }
    }
  }
}
