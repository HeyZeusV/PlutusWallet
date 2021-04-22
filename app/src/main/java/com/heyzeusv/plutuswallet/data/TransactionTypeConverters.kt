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
class TransactionTypeConverters {

    @TypeConverter
    fun fromZonedDateTime(date: ZonedDateTime?): Long? {

        return date?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toZonedDateTime(millisSinceEpoch: Long?): ZonedDateTime? {

        return millisSinceEpoch?.let {
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
    }

    @TypeConverter
    fun fromBigDecimal(total: BigDecimal?): String? {

        return total?.toString()
    }

    @TypeConverter
    fun toBigDecimal(total: String?): BigDecimal? {

        return total?.let { BigDecimal(total) }
    }
}