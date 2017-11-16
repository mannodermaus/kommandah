package de.mannodermaus.kommandah.utils.extensions

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

fun Instant.format(formatter: DateTimeFormatter, zoneId: ZoneId = ZoneId.systemDefault()) =
    this.atZone(zoneId).format(formatter)
