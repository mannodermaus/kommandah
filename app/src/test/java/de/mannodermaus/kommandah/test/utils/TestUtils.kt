package de.mannodermaus.kommandah.test.utils

import de.mannodermaus.kommandah.models.ExitCode
import de.mannodermaus.kommandah.models.Program
import de.mannodermaus.kommandah.models.ProgramException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import java.util.*

/* Helpers */

fun <E> stackOf(vararg args: E) = Stack<E>().apply {
  args.forEach { push(it) }
}

/* Extensions */

inline fun <reified T : ProgramException> Program.assertExecutionFailedWith(message: String? = null) {
  Assertions.assertThat(exitCode)
      .isInstanceOf(ExitCode.Error::class.java)

  val error = exitCode as ExitCode.Error
  Assertions.assertThat(error.cause)
      .isInstanceOf(T::class.java)

  message?.let {
    val cause = error.cause as T
    assertThat(cause.message).isEqualTo(it)
  }
}

fun Program.assertExecutionSuccessful() {
  Assertions.assertThat(this.exitCode).isEqualTo(ExitCode.Success)
}
