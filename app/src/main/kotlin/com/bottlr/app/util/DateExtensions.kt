package com.bottlr.app.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("MMM d", Locale.getDefault())
    .withZone(ZoneId.systemDefault())

fun Instant.toShortDateString(): String = shortDateFormatter.format(this)
