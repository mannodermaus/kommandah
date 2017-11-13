package de.mannodermaus.kommandah.test.mocks.persistence

import de.mannodermaus.kommandah.managers.persistence.Base64Factory
import de.mannodermaus.kommandah.managers.persistence.Base64String
import java.util.*

/**
 * Implementation of a Base64 handler using Java 8's native API.
 */
class Java8Base64Factory : Base64Factory {
  override fun encode(s: String): Base64String =
      Base64.getEncoder().encodeToString(s.toByteArray(Charsets.UTF_8))

  override fun decode(s: Base64String): String =
      Base64.getDecoder().decode(s).toString(Charsets.UTF_8)
}
