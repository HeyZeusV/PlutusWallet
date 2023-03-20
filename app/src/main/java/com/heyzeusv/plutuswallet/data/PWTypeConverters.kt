package com.heyzeusv.plutuswallet.data

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *  Converts complex types.
 *
 *  Room is able to store primitive types with ease, but will have issues with complex
 *  types. Need Type Converters tell Room how to convert complex type to format that
 *  can be stored in database.
 */
class PWTypeConverters {
    @TypeConverter
    fun fromZonedDateTime(date: ZonedDateTime?): Long? = date?.toInstant()?.toEpochMilli()

    @TypeConverter
    fun toZonedDateTime(millisSinceEpoch: Long?): ZonedDateTime? = millisSinceEpoch?.let {
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
    }

    @TypeConverter
    fun fromBigDecimal(total: BigDecimal?): String? = total?.toString()


    @TypeConverter
    fun toBigDecimal(total: String?): BigDecimal? = total?.let { BigDecimal(total) }
}