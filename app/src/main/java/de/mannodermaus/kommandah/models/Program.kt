package de.mannodermaus.kommandah.models

import java.util.*

/**
 * Representation of a Program to be executed by an Interpreter.
 *
 * This class implements Iterable<> in a way that allows its iterator
 * to be usable in a loop, executing each instruction one by one:
 *
 * <code><pre>
 *  val program = Program(instructions)
 *  val iterator = program.iterator()
 *
 *  while (iterator.hasNext()) {
 *    val line = iterator.next()
 *    val result = line.execute()
 *
 *    when (result) {
 *      is Error -> ...
 *      else -> ...
 *    }
 *  }
 * </pre></code>
 */
class Program(instructions: InstructionStack) : Iterable<Program.Line> {

  // Associate each instruction with a reference to the Program
  private val instructions: Lines = instructions.entries
      .associate { it.key to Line(it.value) }

  // Program Counter
  private var pc: Long = 0

  // Destination for pop/push instructions
  private val stack: Stack<Long> = Stack()

  override fun iterator(): Iterator<Line> = ProgramIterator()

  /**
   * Implementor of the Iterable interface for Program objects.
   */
  private inner class ProgramIterator : Iterator<Line> {

    // The iterator will always keep going until stopped by the Interpreter
    override fun hasNext(): Boolean = true

    // Access the next value in the stack
    override fun next(): Line = instructions[pc++] ?: throw IllegalArgumentException("Invalid operation at index ${pc - 1}")
  }

  /**
   * Representation of a single line inside a program,
   * backed by a low-level Instruction.
   */
  inner class Line(private val instruction: Instruction) {
    fun execute(): OutputEvent {
      try {
        when (instruction) {
          is Mult -> {
            // "Pop two values from the stack, multiply them, push the result back"
            val value1 = stack.pop()
            val value2 = stack.pop()
            stack.push(value1 * value2)
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
        return OutputEvent.Tick(instruction)

      } catch (cause: Exception) {
        // Assume construction error by the user
        return OutputEvent.Error(instruction, cause)
      }
    }
  }
}

private typealias Lines = Map<Long, Program.Line>
