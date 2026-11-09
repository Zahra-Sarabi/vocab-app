package com.vocab.core.domain

import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** The clock, injectable so rules that depend on "today" can be tested. */
interface TimeSource {
    fun now(): Long
    fun zone(): ZoneId

    fun epochDay(millis: Long = now()): Long =
        Instant.ofEpochMilli(millis).atZone(zone()).toLocalDate().toEpochDay()
}

class SystemTimeSource @Inject constructor() : TimeSource {
    override fun now(): Long = System.currentTimeMillis()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}

const val MILLIS_PER_MINUTE = 60_000L
const val MILLIS_PER_DAY = 86_400_000L
