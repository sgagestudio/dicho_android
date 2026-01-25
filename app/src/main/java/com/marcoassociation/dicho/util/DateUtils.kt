package com.marcoassociation.dicho.util

import java.time.LocalDate
import java.time.ZoneId

object DateUtils {
    fun monthRangeInMillis(): Pair<Long, Long> {
        val now = LocalDate.now()
        val start = now.withDayOfMonth(1)
        val end = now.withDayOfMonth(now.lengthOfMonth())
        val zone = ZoneId.systemDefault()
        return start.atStartOfDay(zone).toInstant().toEpochMilli() to
            end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
