package de.mannodermaus.kommandah.managers.persistence.impl

import android.util.Base64
import de.mannodermaus.kommandah.managers.persistence.Base64Factory
import de.mannodermaus.kommandah.managers.persistence.Base64String

/**
 * Factory implementation using Android's Base64 API.
 */
class AndroidBase64Factory : Base64Factory {
  override fun encode(s: String): Base64String =
      Base64.encodeToString(s.toByteArray(Charsets.UTF_8), Base64.NO_PADDING)

  override fun decode(s: Base64String): String =
      Base64.decode(s, Base64.NO_PADDING).toString(Charsets.UTF_8)
}
