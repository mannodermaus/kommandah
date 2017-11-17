package de.mannodermaus.kommandah.utils.extensions

import android.content.Context
import android.text.format.DateUtils
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

fun Instant.format(formatter: DateTimeFormatter, zoneId: ZoneId = ZoneId.systemDefault()) =
    this.atZone(zoneId).format(formatter)

fun Instant.toRelativeString(context: Context): CharSequence =
    DateUtils.getRelativeDateTimeString(
        context,
        this.toEpochMilli(),
        DateUtils.DAY_IN_MILLIS,
        DateUtils.WEEK_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_ALL)
