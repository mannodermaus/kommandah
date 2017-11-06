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
 *    val instruction = iterator.next()
 *    val result = instruction.execute()
 *
 *    when (result) {
 *      is Error -> ...
 *      else -> ...
 *    }
 *  }
 * </pre></code>
 */
class Program(private val instructions: InstructionStack) : Iterable<Instruction> {

  // Program Counter
  private var pc: Long = 0

  // Destination for pop/push instructions
  private val stack: Stack<Long> = Stack()

  fun jumpTo(address: Long) {
    this.pc = address
  }

  fun popFromStack(): Long = stack.pop()

  fun pushToStack(first: Long, vararg more: Long) {
    stack.push(first)
    more.forEach { stack.push(it) }
  }

  override fun iterator(): Iterator<Instruction> = ProgramIterator()

  private inner class ProgramIterator : Iterator<Instruction> {

    // The iterator will always keep going until stopped by the Interpreter
    override fun hasNext(): Boolean = true

    // Access the next value in the stack
    override fun next(): Instruction = instructions[pc++] ?: throw IllegalArgumentException("Invalid operation at index ${pc - 1}")
  }
}
