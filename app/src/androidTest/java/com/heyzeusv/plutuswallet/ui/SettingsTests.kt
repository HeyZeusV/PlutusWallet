package com.heyzeusv.plutuswallet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.assertEditTextEquals
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.util.prepareTotalText
import dagger.hilt.android.testing.HiltAndroidTest
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import org.junit.Test

/**
 *  Test device should have default settings
 */
@HiltAndroidTest
class SettingsTests : BaseTest() {

    // only used to get expected values
    private var setVals = SettingsValues()


//    // LanguageTitles in each language available in app other than English
//    private val german = LanguageTitles(
//        "Deutsche", "die Einstellungen", "Überblick",
//        "Transaktion", "Konten", "Kategorien", "Über"
//    )
//    private val spanish = LanguageTitles(
//        "Español (latinoamericano)", "Configuraciones", "Visión general",
//        "Transacción", "Cuentas", "Categorias", "Acerca de"
//    )
//    private val hindi = LanguageTitles(
//        "हिंदी", "समायोजन", "अवलोकन",
//        "लेन-देन", "हिसाब किताब", "श्रेणियाँ", "के बारे में"
//    )
//    private val japanese = LanguageTitles(
//        "日本語", "設定", "概要概要",
//        "トランザクション", "アカウント", "カテゴリー", "約"
//    )
//    private val korean = LanguageTitles(
//        "한국어", "설정", "개요",
//        "트랜잭션", "계정", "카테고리", "약"
//    )
//    private val thai = LanguageTitles(
//        "ไทย", "การตั้งค่า", "ภาพรวม", "การทำธุรกรรม",
//        "บัญชี", "หมวดหมู่", "เกี่ยวกับ"
//    )

    @Test
    fun settings_displaySettings() {
        navigateToSettingsScreen()
    }

    @Test
    fun settings_checkInitialSettings() {
        /**
         *  Checks that the various default Settings are correctly display throughout app.
         *  '$' on left side, ',' used as thousands symbols, '.' used as decimal symbol,
         *  decimals allowed, full date is displayed, and English is language.
         */
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        // check List items
        checkAllTranListItems()

        // navigate to Transaction with id 1
        composeRule.onNode(hasTestTag("${dd.tran1.id}")).performClick()
        // check that we navigate to Transaction screen
        composeRule.onNodeWithText(res.getString(R.string.transaction)).assertExists()
        // check that date and total are formatted correctly
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_date)))
            .assertEditTextEquals(setVals.dateFormatter.format(dd.tran1.date))
        composeRule.onNode(hasTestTag(res.getString(R.string.transaction_total)))
            .assertEditTextEquals("\$${setVals.decimalFormatter.format(dd.tran1.total)}")
        Espresso.pressBack()

        // check other screens and their titles
        navigateToDrawerScreenAndCheckTitle("Accounts", false)
        navigateToDrawerScreenAndCheckTitle("Categories", false)
        navigateToDrawerScreenAndCheckTitle("About", false)
        navigateToSettingsScreen()
    }

    @Test
    fun settings_changeSymbols() {
        // navigate to Settings, change symbols then go back and check
        navigateToSettingsScreen()
        selectSymbolSettings("€", '-', ' ')
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change symbols then go back and check
        navigateToSettingsScreen()
        selectSymbolSettings("¥", '.', '-')
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change symbols then go back and check
        navigateToSettingsScreen()
        selectSymbolSettings("£", ' ', ',')
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change symbols then go back and check
        navigateToSettingsScreen()
        selectSymbolSettings("₩", ' ', ',')
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change symbols then go back and check
        navigateToSettingsScreen()
        selectSymbolSettings("฿", ' ', ',')
        Espresso.pressBack()
        checkAllTranListItems()
    }

    @Test
    fun settings_changeDateFormat() {
        // navigate to Settings, change date format then go back and check
        navigateToSettingsScreen()
        selectOptionInSetting("DATE_FORMAT", "April 19, 1993")
        setVals = setVals.copy(dateFormatter = DateFormat.getDateInstance(1))
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change date format then go back and check
        navigateToSettingsScreen()
        selectOptionInSetting("DATE_FORMAT", "Apr 19, 1993")
        setVals = setVals.copy(dateFormatter = DateFormat.getDateInstance(2))
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change date format then go back and check
        navigateToSettingsScreen()
        selectOptionInSetting("DATE_FORMAT", "4/19/93")
        setVals = setVals.copy(dateFormatter = DateFormat.getDateInstance(3))
        Espresso.pressBack()
        checkAllTranListItems()
    }

    @Test
    fun settings_swapThousandsDecimalSymbols() {
        navigateToSettingsScreen()

        // select same option as decimal symbol
        composeRule.onNode(hasTestTag("THOUSANDS_SYMBOL")).performClick()
        composeRule.onNode(hasTestTag("\".\""), useUnmergedTree = true).performClick()
        composeRule.onNode(hasTestTag("AlertDialog confirm"), useUnmergedTree = true).performClick()
        // confirm switch
        composeRule.onNode(hasTestTag("AlertDialog confirm"), useUnmergedTree = true).performClick()

        // confirm that symbols switched positions
        composeRule.onNode(hasTestTag("THOUSANDS_SYMBOL \".\""), useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNode(hasTestTag("DECIMAL_SYMBOL \",\""), useUnmergedTree = true)
            .assertIsDisplayed()

        // check TranListItems
        updateTotalFormatters("$", '.', ',')
        Espresso.pressBack()
        checkAllTranListItems()
    }

    // Need to look into this test and language changing
//    @Test
//    fun settings_changeTheme() {
//        navigateToSettingsScreen()
//
//        selectOptionInSetting("THEME", "Light")
//        selectOptionInSetting("THEME", "Dark")
//        selectOptionInSetting("THEME", "System")
//    }

    private fun navigateToSettingsScreen() {
        // check that we start on Overview screen, open drawer, and navigate to Settings screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_drawer_description)))
            .performClick()
        composeRule.onNode(hasTestTag("DrawerItem Settings")).performClick()

        // check that we navigate to Account screen
        composeRule.onNode(hasTestTag("AppBar Settings")).assertExists()
    }

    /**
     *  Navigates to a screen that requires drawer input. It does this by using [title] to determine
     *  which item to select, [translated] to know if [translatedTitle] needs to be check as well
     *  once arrived at destination.
     */
    private fun navigateToDrawerScreenAndCheckTitle(
        title: String,
        translated: Boolean = true,
        translatedTitle: String = ""
    ) {
        // open drawer and click on item with title
        composeRule.onNode(hasContentDescription(res.getString(R.string.cfl_drawer_description)))
            .performClick()
        composeRule.onNode(hasTestTag("DrawerItem $title")).performClick()

        if (translated) {
            // check that title is correctly translated
            composeRule.onNodeWithText(translatedTitle).assertExists()
        }
        composeRule.onNode(hasTestTag("AppBar $title")).assertExists()

        // return to Overview screen
        Espresso.pressBack()
    }

    /**
     *  Checks that [tran] has its date and total strings formatted correctly.
     */
    private fun checkTranListItemDateTotal(tran: Transaction) {
        val totalText = tran.total.prepareTotalText(setVals)

        // checks that total and date are formatted correctly
        composeRule.onNodeWithText(setVals.dateFormatter.format(tran.date)).assertExists()
        composeRule.onNodeWithText(text = totalText, useUnmergedTree = true).assertExists()
    }

    /**
     *  Calls [checkTranListItemDateTotal] on all Transactions in dummy data
     */
    private fun checkAllTranListItems() = dd.tranList.forEach { checkTranListItemDateTotal(it) }

    /**
     *  Updates setVal symbols/formatters using [currencySymbol], [decimalSymbol],
     *  and [thousandsSymbol].
     */
    private fun updateTotalFormatters(
        currencySymbol: String,
        thousandsSymbol: Char,
        decimalSymbol: Char
    ) {
        val customSymbols = DecimalFormatSymbols(Locale.US)
        customSymbols.groupingSeparator = thousandsSymbol
        customSymbols.decimalSeparator = decimalSymbol

        setVals = setVals.copy(
            currencySymbol = currencySymbol,
            thousandsSymbol = thousandsSymbol,
            decimalSymbol = decimalSymbol,
            decimalFormatter = DecimalFormat("#,###.00", customSymbols).apply {
                roundingMode = RoundingMode.HALF_UP
            },
            integerFormatter = DecimalFormat("#,###", customSymbols).apply {
                roundingMode = RoundingMode.HALF_UP
            }
        )
    }

    /**
     *  Selects [setting] to open its Dialog, then selects [option] from list and confrims selection
     */
    private fun selectOptionInSetting(setting: String, option: String) {
        composeRule.onNode(hasTestTag(setting)).performClick()
        composeRule.onNode(hasTestTag(option), useUnmergedTree = true).performClick()
        composeRule.onNode(hasTestTag("AlertDialog confirm"), useUnmergedTree = true).performClick()
        composeRule.onNode(hasTestTag("$setting $option"), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    /**
     *  Uses [selectOptionInSetting] to update [currency], [thousands], and [decimal] symbols
     *  together. Lastly, calls [updateTotalFormatters] to update setVals with new symbols.
     */
    private fun selectSymbolSettings(currency: String, thousands: Char, decimal: Char) {
        selectOptionInSetting("CURRENCY_SYMBOL", currency)
        selectOptionInSetting("THOUSANDS_SYMBOL", "\"$thousands\"")
        selectOptionInSetting("DECIMAL_SYMBOL", "\"$decimal\"")
        updateTotalFormatters(currency, thousands, decimal)
    }
}

///**
// *  Data class holding translated Screen strings and the [language] itself.
// */
//data class LanguageTitles(
//    val language: String,
//    val settingsTitle: String,
//    val cflTitle: String,
//    val tranTitle: String,
//    val accTitle: String,
//    val catTitle: String,
//    val aboutTitle: String
//)