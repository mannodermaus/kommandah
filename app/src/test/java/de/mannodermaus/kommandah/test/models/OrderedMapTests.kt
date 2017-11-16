package de.mannodermaus.kommandah.test.models

import de.mannodermaus.kommandah.models.OrderedMap
import de.mannodermaus.kommandah.test.utils.extensions.mappedTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderedMapTests {

  private lateinit var map: OrderedMap<String>

  @BeforeEach
  fun beforeEach() {
    map = OrderedMap()
  }

  @Test
  fun appendWorksAsExpected() {
    map.append("First")
    map.append("Second")

    assertThat(map).containsOnly(
        0 mappedTo "First",
        1 mappedTo "Second"
    )
  }

  @Test
  fun swapWorksAsExpected() {
    map.put(0, "First")
    map.put(1, "Second")
    map.put(2, "Third")
    map.put(3, "Fourth")
    map.swap(0, 1)

    assertThat(map).containsOnly(
        0 mappedTo "Second",
        1 mappedTo "First",
        2 mappedTo "Third",
        3 mappedTo "Fourth"
    )
  }

  @Test
  fun swapWithNullAndAppendIntegration() {
    map.put(1, "Second")
    map.put(2, "Third")
    map.swap(0, 2)
    map.append("New Item")

    assertThat(map).containsOnly(
        0 mappedTo "Third",
        1 mappedTo "Second",
        2 mappedTo "New Item"
    )
  }

  @Test
  fun swapWithNullValue() {
    map.put(0, "First")
    map.swap(0, 1)

    assertThat(map).containsOnly(
        1 mappedTo "First"
    )
  }

  @Test
  fun appendWithGapsWorksAsExpected() {
    map.put(0, "First")
    map.put(4, "Second")
    map.append("Third")

    assertThat(map).containsOnly(
        0 mappedTo "First",
        4 mappedTo "Second",
        5 mappedTo "Third")
  }

  @Test
  fun insertBeforeWorksAsExpected() {
    map.put(0, "First")
    map.put(1, "Second")
    map.put(2, "Third")
    map.insertBefore(1, "New Item")

    assertThat(map).containsOnly(
        0 mappedTo "First",
        1 mappedTo "New Item",
        2 mappedTo "Second",
        3 mappedTo "Third"
    )
  }

  @Test
  fun insertBeforeWithGapsWorksAsExpected() {
    map.put(0, "First")
    map.put(1, "Second")
    map.put(3, "Third")
    map.put(5, "Fourth")
    map.insertBefore(1, "New Item")

    assertThat(map).containsOnly(
        0 mappedTo "First",
        1 mappedTo "New Item",
        2 mappedTo "Second",
        3 mappedTo "Third",
        5 mappedTo "Fourth"
    )
  }

  @Test
  fun nullPaddedValuesWorksAsExpected() {
    map.put(0, "First")
    map.put(1, "Second")
    map.put(3, "Third")
    val values = map.nullPaddedValues

    assertThat(values).containsExactly(
        "First",
        "Second",
        null,
        "Third")
  }

  @Test
  fun removeWorksAsExpected() {
    map.put(0, "First")
    map.put(1, "Second")
    map.put(2, "Third")
    map.put(3, "Fourth")
    map.remove(1)

    assertThat(map).containsOnly(
        0 mappedTo "First",
        1 mappedTo "Third",
        2 mappedTo "Fourth"
    )
  }

  @Test
  fun removeWithGapsWorksAsExpected() {
    map.put(0, "First")
    map.put(1, "Second")
    map.put(2, "Third")
    map.put(5, "Fourth")
    map.remove(1)

    assertThat(map).containsOnly(
        0 mappedTo "First",
        1 mappedTo "Third",
        5 mappedTo "Fourth"
    )
  }

  @Test
  fun removeWithSingleItemGapWorksAsExpected() {
    map.put(0, "First")
    map.put(1, "Second")
    map.put(3, "Third")
    map.put(5, "Fourth")
    map.remove(1)

    assertThat(map).containsOnly(
        0 mappedTo "First",
        3 mappedTo "Third",
        5 mappedTo "Fourth"
    )
  }

  @Test
  fun removeWithBlockWorksAsExpected() {
    map.put(0, "First")
    map.put(1, "Second")
    map.put(3, "Third")
    map.put(5, "Fourth")
    map.remove(1)

    assertThat(map).containsOnly(
        0 mappedTo "First",
        3 mappedTo "Third",
        5 mappedTo "Fourth"
    )
  }

  @Test
  fun removeThenAppendWorksAsExpected() {
    map.put(0, "First")
    map.put(1, "Second")
    map.put(2, "Third")
    map.remove(1)
    map.append("New Item")

    assertThat(map).containsOnly(
        0 mappedTo "First",
        1 mappedTo "Third",
        2 mappedTo "New Item"
    )
  }
}
