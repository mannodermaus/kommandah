package de.mannodermaus.kommandah.managers.persistence

typealias Base64String = String

interface Base64Factory {
  fun encode(s: String): Base64String
  fun decode(s: Base64String): String
}
