package de.mannodermaus.kommandah.test.utils.extensions

import org.assertj.core.api.Assertions

infix fun <K, V> K.mappedTo(value: V): Map.Entry<K, V> =
    Assertions.entry(this, value)
