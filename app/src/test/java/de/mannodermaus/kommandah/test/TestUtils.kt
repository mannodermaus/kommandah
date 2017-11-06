package de.mannodermaus.kommandah.test

import de.mannodermaus.kommandah.models.ExitCode
import de.mannodermaus.kommandah.models.Program
import org.assertj.core.api.Assertions
import java.util.*

/* Helpers */

fun <E> stackOf(vararg args: E) = Stack<E>().apply {
  args.forEach { push(it) }
}

/* Extensions */

inline fun <reified T : Exception> Program.assertExecutionFailedWith() {
  Assertions.assertThat(this.exitCode())
      .isInstanceOf(ExitCode.Error::class.java)
      .extracting("cause")
      .hasOnlyOneElementSatisfying { it is T }
}

fun Program.assertExecutionSuccessful() {
  Assertions.assertThat(this.exitCode()).isEqualTo(ExitCode.Success)
}
