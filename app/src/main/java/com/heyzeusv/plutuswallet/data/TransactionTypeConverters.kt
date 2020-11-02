package com.heyzeusv.plutuswallet.data

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.util.Date

/**
 *  Converts complex types.
 *
 *  Room is able to store primitive types with ease, but will have issues with complex
 *  types. Need Type Converters tell Room how to convert complex type to format that
 *  can be stored in database.
 */
class TransactionTypeConverters {

    @TypeConverter
    fun fromDate(date: Date?): Long? {

        return date?.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {

        return millisSinceEpoch?.let { Date(it) }
    }

    @TypeConverter
    fun fromBigDecimal(total: BigDecimal?): String? {

        return total?.toString()
    }

    @TypeConverter
    fun toBigDecimal(total: String?): BigDecimal? {

        return BigDecimal(total)
    }
}