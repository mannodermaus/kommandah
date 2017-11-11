package de.mannodermaus.kommandah.utils.extensions

import io.michaelrocks.bimap.HashBiMap

inline fun <K : Any, V : Any> bimapOf(vararg values: Pair<K, V>) =
    HashBiMap<K, V>(capacity = values.size).apply { putAll(values) }
