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
class PWTypeConverters {

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? = millisSinceEpoch?.let { Date(it) }


    @TypeConverter
    fun fromBigDecimal(total: BigDecimal?): String? = total?.toString()


    @TypeConverter
    fun toBigDecimal(total: String?): BigDecimal? = total?.let { BigDecimal(total) }
}