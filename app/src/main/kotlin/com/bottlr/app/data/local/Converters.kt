@file:Suppress("unused")

package com.bottlr.app.data.local

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Room TypeConverters for custom types.
 * These look unused but Room uses them via reflection at compile time.
 */
class Converters {

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(epochMilli: Long?): Instant? {
        return epochMilli?.let { Instant.ofEpochMilli(it) }
    }
}
