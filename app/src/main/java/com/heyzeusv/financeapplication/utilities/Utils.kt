package com.heyzeusv.financeapplication.utilities

class Utils {

    companion object {

        /**
         *  Used to get Currency symbol to be displayed depending on users selection
         *
         *  @param  symbolKey taken from SettingsFragment
         *  @return the symbol to be used
         */
        fun getSymbol(symbolKey : String) : String {

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
    }
}
