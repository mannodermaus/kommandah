package de.mannodermaus.kommandah.test

import java.util.*

fun <E> stackOf(vararg args: E) = Stack<E>().apply {
  args.forEach { push(it) }
}
