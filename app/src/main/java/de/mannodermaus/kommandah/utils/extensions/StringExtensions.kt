package de.mannodermaus.kommandah.utils.extensions

import de.mannodermaus.kommandah.managers.persistence.Base64Factory

/* Extension Functions */

fun String.encodeToBase64(factory: Base64Factory): String =
    factory.encode(this)

fun String.decodeFromBase64(factory: Base64Factory): String =
    factory.decode(this)
