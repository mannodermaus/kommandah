package de.mannodermaus.kommandah.test.utils.extensions

import io.reactivex.observers.TestObserver

fun <T> TestObserver<T>.assertLatestValue(predicate: (T) -> Boolean) =
    assertValueAt(valueCount() - 1, predicate)
