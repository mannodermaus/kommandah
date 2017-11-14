package de.mannodermaus.kommandah.models

class OrderedMap<T> : LinkedHashMap<Int, T>() {

  private var biggestKey: Int = 0

  /* New API */

  /**
   * Returns a padded list of the values contained in this Map,
   * up to the highest entry. Gaps in the Map are filled with null values.
   *
   * Example:
   * val map = OrderedMap<String>()
   * map.put(0, "First")
   * map.put(1, "Second")
   * map.put(3, "Third")
   *
   * val values = map.nullPaddedValues()
   * values.forEach { println(it) }
   *
   * ->
   * "First"
   * "Second"
   * null
   * "Third"
   */
  fun nullPaddedValues(): List<T?> = (0 until biggestKey).map { this[it] }

  fun append(value: T): T? = put(biggestKey, value)

  fun insertBefore(key: Int, value: T) {
    val current = this[key]
    if (current == null) {
      // No value currently at this position.
      // Occupy the spot directly
      put(key, value)

    } else {
      // Another value is already present.
      // Move it over to the next spot
      val next = this[key + 1]
      if (next == null) {
        // Neighbor is empty, simply reassign
        put(key + 1, current)
        put(key, value)

      } else {
        // Recursively move the remaining items out of the way
        insertBefore(key + 1, current)
        put(key, value)
      }
    }
  }

  fun swap(fromKey: Int, toKey: Int) {
    val fromValue = super.remove(fromKey)
    val toValue = super.remove(toKey)

    if (fromValue != null) put(toKey, fromValue)
    if (toValue != null) put(fromKey, toValue)

    // Update biggest key, since we might have swapped a null value to the end of the list
    biggestKey = 1 + (keys.sortedDescending().firstOrNull() ?: 0)
  }

  fun forEachIndexed(func: (Int, T) -> Unit) {
    entries.forEach { func.invoke(it.key, it.value) }
  }

  /* Operators */

  operator fun plusAssign(value: T) {
    append(value)
  }

  /* Overrides */

  override fun put(key: Int, value: T): T? {
    // If the addition exceeds the previously "biggest" value,
    // update the indicator for the biggest key
    if (key >= biggestKey) {
      biggestKey = key + 1
    }

    return super.put(key, value)
  }

  override fun remove(key: Int): T? {
    val removed = super.remove(key)

    // Move over the associated block of values "from the right"
    // to fill the newly created gap
    for (movedKey in (key + 1)..biggestKey) {
      val next = this[movedKey] ?: break
      super.remove(movedKey)
      this[movedKey - 1] = next

      // If the removal is moving the previously "biggest" value,
      // also update the indicator for the biggest key
      if (movedKey == biggestKey - 1) {
        biggestKey = movedKey
      }
    }

    return removed
  }
}
