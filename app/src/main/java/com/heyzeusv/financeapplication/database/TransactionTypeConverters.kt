package com.heyzeusv.financeapplication.database

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.util.*

/**
 *  Converts complex types.
 *
 *  Room is able to store primitive types with ease, but will have issues with complex
 *  types. Need Type Converters tell Room how to convert complex type to format that
 *  can be stored in database.
 */
class TransactionTypeConverters {

    @TypeConverter
    fun fromDate(date : Date?) : Long? {

        return date?.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch : Long?) : Date? {

        return millisSinceEpoch?.let {

            Date(it)
        }
    }

    @TypeConverter
    fun fromBigDecimal(total : BigDecimal?) : Float? {

        return total?.toFloat()
    }

    @TypeConverter
    fun toBigDecimal(total : Float?) : BigDecimal? {

        return BigDecimal(total.toString())
    }
}