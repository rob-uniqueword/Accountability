package com.rob_uniqueword.accountability

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

fun TemporalAccessor.toDateString(): String = dateFormatter.format(this)
fun TemporalAccessor.toTimeString(): String = timeFormatter.format(this)
fun TemporalAccessor.toDateTimeString(): String = dateTimeFormatter.format(this)