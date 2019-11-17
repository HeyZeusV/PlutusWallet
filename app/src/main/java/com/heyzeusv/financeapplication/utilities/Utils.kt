package com.heyzeusv.financeapplication.utilities

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.*

/**
 *  Functions used in multiple classes
 */
class Utils {

    companion object {

        /**
         *  Changes locale depending on users selection.
         *
         *  @param  context      used to change configuration.
         *  @param  languageCode string containing language code to be used. eg "en", "es".
         *  @return holds context that contains updated locale.
         */
        fun changeLanguage(context : Context, languageCode : String) : ContextWrapper {

            val config     : Configuration = context.resources.configuration
            val newContext : Context
            val newLocale                  = Locale(languageCode)

            // sets locale for JVM
            Locale.setDefault(newLocale)
            // sets locale for context
            config.setLocale(newLocale)
            // new context needed since parameters are final
            newContext = context.createConfigurationContext(config)

            return ContextWrapper(newContext)
        }

        /**
         *  Used to get Currency symbol to be displayed depending on users selection.
         *
         *  @param  symbolKey taken from SettingsFragment.
         *  @return the symbol to be used.
         */
        fun getCurrencySymbol(symbolKey : String) : String {

            return when (symbolKey) {

                "dollar" -> "$"
                "euro"   -> "€"
                "pound"  -> "£"
                "yen"    -> "¥"
                "rupee"  -> "₹"
                "won"    -> "₩"
                "baht"   -> "฿"
                else     -> "$"
            }
        }

        /**
         *  Used to get Separator symbol to be displayed depending on users selection.
         *
         *  @param  symbolKey taken from SettingsFragment.
         *  @return the symbol to be used.
         */
        fun getSeparatorSymbol(symbolKey : String) : Char {

            return when (symbolKey) {

                "comma"  -> ','
                "period" -> '.'
                else     -> ' '
            }
        }

        /**
         *  Sets time right that the beginning at the day.
         *
         *  @param  date the date object to be changed.
         *  @return Date object with time at start of day.
         */
        fun startOfDay(date : Date) : Date {

            val calendar = GregorianCalendar()
            calendar.timeInMillis = date.time
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE     , 0)
            calendar.set(Calendar.SECOND     , 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar.time
        }
    }
}
