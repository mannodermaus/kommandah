package de.mannodermaus.kommandah.models

/**
 * Custom Map implementation which combines the expected "put/remove" operations
 * with those prevalent in Lists, like [append] and [swap].
 *
 * Using this class, you can model "indexed lists with gaps".
 */
class OrderedMap<T> : LinkedHashMap<Int, T>() {

  private var highestPosition: Int = 0

  /* New API */

  /**
   * Returns the values contained in this Map up to the highest entry,
   * padded with null values that represent gaps in the Map.
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
  val nullPaddedValues: List<T?>
    get() = (0 until highestPosition).map { this[it] }

  /**
   * Appends the given value to the end of the Map, i.e. at the highest index.
   */
  fun append(value: T): T? = put(highestPosition, value)

  /**
   * Inserts the given value at the provided position.
   * Moves any existing block of values "out of the way" to the right,
   * so that the new value fits into the original position of the first element in the block.
   */
  fun insertBefore(position: Int, value: T) {
    val current = this[position]
    if (current == null) {
      // No value currently at this position.
      // Occupy the spot directly
      put(position, value)

    } else {
      // Another value is already present.
      // Move it over to the next spot
      val next = this[position + 1]
      if (next == null) {
        // Neighbor is empty, simply reassign
        put(position + 1, current)
        put(position, value)

      } else {
        // Recursively move the remaining items out of the way
        insertBefore(position + 1, current)
        put(position, value)
      }
    }
  }

  /**
   * Swaps the values at the given positions.
   * If any position doesn't hold a value,
   * the corresponding null is still swapped to the other position.
   */
  fun swap(fromPosition: Int, toPosition: Int) {
    val fromValue = super.remove(fromPosition)
    val toValue = super.remove(toPosition)

    if (fromValue != null) put(toPosition, fromValue)
    if (toValue != null) put(fromPosition, toValue)

    // Update the highest position,
    // since we might have swapped a null value to the end of the list
    highestPosition = 1 + (keys.sortedDescending().firstOrNull() ?: 0)
  }

  /**
   * Applies the provided function to each present item in the Map.
   */
  fun forEachIndexed(func: (Int, T) -> Unit) {
    entries.forEach { func.invoke(it.key, it.value) }
  }

  /* Operators */

  operator fun plusAssign(value: T) {
    append(value)
  }

  /* Overrides */

  @Deprecated(
      message = "Consider using nullPaddedValues instead.",
      replaceWith = ReplaceWith("nullPaddedValues"),
      level = DeprecationLevel.WARNING)
  override val values: MutableCollection<T>
    get() = super.values

  override fun put(key: Int, value: T): T? {
    // If the addition exceeds the previously "biggest" value,
    // update the indicator for the biggest key
    if (key >= highestPosition) {
      highestPosition = key + 1
    }

    return super.put(key, value)
  }

  override fun remove(key: Int): T? {
    val removed = super.remove(key)

    // Move over the associated block of values "from the right"
    // to fill the newly created gap
    for (movedKey in (key + 1)..highestPosition) {
      val next = this[movedKey] ?: break
      super.remove(movedKey)
      this[movedKey - 1] = next

      // If the removal is moving the previously "biggest" value,
      // also update the indicator for the biggest key
      if (movedKey == highestPosition - 1) {
        highestPosition = movedKey
      }
    }

    return removed
  }
}
