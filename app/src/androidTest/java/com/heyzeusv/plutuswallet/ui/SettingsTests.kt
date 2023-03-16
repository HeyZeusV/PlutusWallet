package com.heyzeusv.plutuswallet.ui

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.assertBackgroundColor
import com.heyzeusv.plutuswallet.assertEditTextEquals
import com.heyzeusv.plutuswallet.data.DummyAndroidDataUtil
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.onNodeWithContDiscId
import com.heyzeusv.plutuswallet.onNodeWithTTStrId
import com.heyzeusv.plutuswallet.onNodeWithTextId
import com.heyzeusv.plutuswallet.util.prepareTotalText
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.util.theme.Purple900
import com.heyzeusv.plutuswallet.util.theme.Purple900Light
import com.heyzeusv.plutuswallet.util.theme.PurpleBase
import com.heyzeusv.plutuswallet.util.theme.PurpleDark
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 *  Test device should have default settings
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsTests {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeRule = createAndroidComposeRule<MainActivity>()

    // only used to get expected values
    private var setVals = SettingsValues()
    val dd = DummyAndroidDataUtil()

    @Before
    fun setUp() {
        composeRule.activity.setContent {
            PlutusWalletTheme {
                PlutusWalletApp()
            }
        }
    }

    @Test
    fun settings_displaySettings() {
        navigateToSettingsScreenFromOverview()
    }

    @Test
    fun settings_checkInitialSettings() {
        /**
         *  Checks that the various default Settings are correctly display throughout app.
         *  '$' on left side, ',' used as thousands symbols, '.' used as decimal symbol,
         *  decimals allowed, full date is displayed, and English is language.
         */
        composeRule.onNodeWithTextId(R.string.cfl_overview).assertExists()
        // check List items
        checkAllTranListItems()

        // navigate to Transaction with id 1
        composeRule.onNodeWithTTStrId(R.string.tt_tranL_item, dd.tran1.id).performClick()
        // check that we navigate to Transaction screen
        composeRule.onNodeWithTextId(R.string.transaction).assertExists()
        // check that date and total are formatted correctly
        composeRule.onNodeWithTTStrId(R.string.transaction_date)
            .assertEditTextEquals(setVals.dateFormatter.format(dd.tran1.date))
        composeRule.onNodeWithTTStrId(R.string.transaction_total)
            .assertEditTextEquals("\$${setVals.decimalFormatter.format(dd.tran1.total)}")
        Espresso.pressBack()

        // check other screens and their titles
        navigateToDrawerScreenAndCheckTitle("Accounts", false)
        navigateToDrawerScreenAndCheckTitle("Categories", false)
        navigateToDrawerScreenAndCheckTitle("About", false)
        navigateToSettingsScreenFromOverview()
    }

    /**
     *  Theme test checks the background color of the AppBar and Scaffold Composable that the entire
     *  app uses in order to check that the Theme changes correctly.
     */
    @Test
    fun settings_changeTheme() {
        navigateToSettingsScreenFromOverview()
        selectOptionInSetting("THEME", "Light", false)
        composeRule.onNodeWithTTStrId(R.string.tt_app_scaffold).assertBackgroundColor(Purple900Light)
        composeRule.onNodeWithTTStrId(R.string.tt_app_bar).assertBackgroundColor(Purple900)

        navigateToSettingsScreenFromOverview()
        selectOptionInSetting("THEME", "Dark", false)
        composeRule.onNodeWithTTStrId(R.string.tt_app_scaffold).assertBackgroundColor(PurpleDark)
        composeRule.onNodeWithTTStrId(R.string.tt_app_bar).assertBackgroundColor(PurpleBase)

        // for some reason the second theme switch does not send user to Overview screen, but stays
        // on Setting screen, this only happens in testing
        selectOptionInSetting("THEME", "System", false)
        composeRule.onNodeWithTTStrId(R.string.tt_app_scaffold).assertBackgroundColor(Purple900Light)
        composeRule.onNodeWithTTStrId(R.string.tt_app_bar).assertBackgroundColor(Purple900)
    }

    @Test
    fun settings_changeSymbols() {
        // navigate to Settings, change symbols then go back and check
        navigateToSettingsScreenFromOverview()
        selectSymbolSettings("€", '-', ' ')
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change symbols then go back and check
        navigateToSettingsScreenFromOverview()
        selectSymbolSettings("¥", '.', '-')
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change symbols then go back and check
        navigateToSettingsScreenFromOverview()
        selectSymbolSettings("£", ' ', ',')
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change symbols then go back and check
        navigateToSettingsScreenFromOverview()
        selectSymbolSettings("₩", ' ', ',')
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change symbols then go back and check
        navigateToSettingsScreenFromOverview()
        selectSymbolSettings("฿", ' ', ',')
        Espresso.pressBack()
        checkAllTranListItems()
    }

    @Test
    fun settings_changeDateFormat() {
        // navigate to Settings, change date format then go back and check
        navigateToSettingsScreenFromOverview()
        selectOptionInSetting("DATE_FORMAT", "April 19, 1993")
        setVals = setVals.copy(dateFormatter = DateFormat.getDateInstance(1))
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change date format then go back and check
        navigateToSettingsScreenFromOverview()
        selectOptionInSetting("DATE_FORMAT", "Apr 19, 1993")
        setVals = setVals.copy(dateFormatter = DateFormat.getDateInstance(2))
        Espresso.pressBack()
        checkAllTranListItems()

        // navigate to Settings, change date format then go back and check
        navigateToSettingsScreenFromOverview()
        selectOptionInSetting("DATE_FORMAT", "4/19/93")
        setVals = setVals.copy(dateFormatter = DateFormat.getDateInstance(3))
        Espresso.pressBack()
        checkAllTranListItems()
    }

    @Test
    fun settings_swapThousandsDecimalSymbols() {
        navigateToSettingsScreenFromOverview()

        // select same option as decimal symbol
        composeRule.onNodeWithTTStrId(R.string.tt_set_name, "THOUSANDS_SYMBOL").performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_ad_option, "\".\"", useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_ad_confirm, useUnmergedTree = true).performClick()
        // confirm switch
        composeRule.onNodeWithTTStrId(R.string.tt_ad_confirm, useUnmergedTree = true).performClick()

        // confirm that symbols switched positions
        composeRule.onNodeWithTTStrId(
            R.string.tt_set_select, "THOUSANDS_SYMBOL",  "\".\"", useUnmergedTree = true
        ).assertIsDisplayed()
        composeRule.onNodeWithTTStrId(
            R.string.tt_set_select, "DECIMAL_SYMBOL", "\",\"", useUnmergedTree = true
        ).assertIsDisplayed()

        // check TranListItems
        updateTotalFormatters("$", '.', ',')
        Espresso.pressBack()
        checkAllTranListItems()
    }

    @Test
    fun settings_changeLanguage() {
        navigateToSettingsScreenFromOverview()
        selectOptionInSetting("LANGUAGE", german.language, false)
        navigateToSettingsScreenFromOverview(german)
        navigateToScreensAndCheckTitleFromSettings(german)
        navigateToSettingsScreenFromOverview(german)

        selectOptionInSetting("LANGUAGE", spanish.language, false)
        navigateToScreensAndCheckTitleFromSettings(spanish)
        navigateToSettingsScreenFromOverview(spanish)

        selectOptionInSetting("LANGUAGE", hindi.language, false)
        navigateToScreensAndCheckTitleFromSettings(hindi)
        navigateToSettingsScreenFromOverview(hindi)

        selectOptionInSetting("LANGUAGE", japanese.language, false)
        navigateToScreensAndCheckTitleFromSettings(japanese)
        navigateToSettingsScreenFromOverview(japanese)

        selectOptionInSetting("LANGUAGE", korean.language, false)
        navigateToScreensAndCheckTitleFromSettings(korean)
        navigateToSettingsScreenFromOverview(korean)

        selectOptionInSetting("LANGUAGE", thai.language, false)
        navigateToScreensAndCheckTitleFromSettings(thai)
        navigateToSettingsScreenFromOverview(thai)

        selectOptionInSetting("LANGUAGE", english.language, false)
        navigateToScreensAndCheckTitleFromSettings(english)
        navigateToSettingsScreenFromOverview(english)
    }

    /**
     *  Navigates to all screens available and checks that their titles match with given [language].
     */
    private fun navigateToScreensAndCheckTitleFromSettings(language: LanguageStrings) {
        // check that we start on Settings screen
        composeRule.onNodeWithTTStrId(R.string.tt_app_barTitle, language.settingsTitle).assertIsDisplayed()
        // navigate to Overview screen and check title
        composeRule.onNode(hasContentDescription(language.navContDesc)).performClick()
        composeRule.onNodeWithText(language.overviewTitle).assertIsDisplayed()
        // navigate to Transaction screen and check title
        composeRule.onNode(hasContentDescription(language.tranContDesc)).performClick()
        composeRule.onNodeWithText(language.tranTitle).assertIsDisplayed()
        composeRule.onNode(hasContentDescription(language.navContDesc)).performClick()
        // navigate to Account screen and check title
        navigateToDrawerScreenAndBackFromOverview(language, language.accTitle)
        // navigate to Categories screen and check title
        navigateToDrawerScreenAndBackFromOverview(language, language.catTitle)
        // navigate to About screen and check title
        navigateToDrawerScreenAndBackFromOverview(language, language.aboutTitle)
        // check that we finish on Overview screen
        composeRule.onNodeWithText(language.overviewTitle).assertIsDisplayed()
    }

    /**
     *  Navigates to a [screen] that is available from Drawer using given [language] to check
     *  that correct language is being displayed.
     */
    private fun navigateToDrawerScreenAndBackFromOverview(language: LanguageStrings, screen: String) {
        composeRule.onNode(hasContentDescription(language.drawerContDesc)).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_app_drawItem, screen).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_app_barTitle, screen).assertIsDisplayed()
        composeRule.onNode(hasContentDescription(language.navContDesc)).performClick()
    }

    /**
     *  Navigates to Settings screen starting from Overview screen using given [language].
     */
    private fun navigateToSettingsScreenFromOverview(language: LanguageStrings = english) {
        // check that we start on Overview screen, open drawer, and navigate to Settings screen
        composeRule.onNodeWithText(language.overviewTitle).assertIsDisplayed()
        composeRule.onNode(hasContentDescription(language.drawerContDesc)).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_app_drawItem, language.settingsTitle)
            .performClick()

        // check that we navigate to Account screen
        composeRule.onNodeWithTTStrId(R.string.tt_app_barTitle, language.settingsTitle)
            .assertIsDisplayed()
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
        composeRule.onNodeWithContDiscId(R.string.cfl_drawer_description)
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_app_drawItem, title).performClick()

        if (translated) {
            // check that title is correctly translated
            composeRule.onNodeWithText(translatedTitle).assertExists()
        }
        composeRule.onNodeWithTTStrId(R.string.tt_app_barTitle, title).assertExists()

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
     *  Selects [setting] to open its Dialog, then selects [option] from list, and confirms
     *  selection if [check] is true.
     */
    private fun selectOptionInSetting(setting: String, option: String, check: Boolean = true) {
        composeRule.onNodeWithTTStrId(R.string.tt_set_name, setting).performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_ad_option, option, useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithTTStrId(R.string.tt_ad_confirm, useUnmergedTree = true).performClick()
        if (check) {
            composeRule.onNodeWithTTStrId(
                R.string.tt_set_select, setting, option, useUnmergedTree = true
            ).assertIsDisplayed()
        }
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

    // LanguageStrings in each language available in app
    private val english = LanguageStrings(
        "English", "Settings", "Overview", "Transaction", "Accounts",
        "Categories", "About", "Open Navigation Drawer", "Navigate Back", "New Transaction"
    )
    private val german = LanguageStrings(
        "Deutsche", "die Einstellungen", "Überblick", "Transaktion", "Konten", "Kategorien",
        "Über", "Öffnen Sie die Navigationsleiste", "Navigieren Sie zurück", "Neue Transaktion"
    )
    private val spanish = LanguageStrings(
        "Español (latino americano)", "Configuraciones", "Visión general",
        "Transacción", "Cuentas", "Categorias", "Acerca de", "Abrir cajón de navegación",
        "Navegar hacia atrás", "Nueva transacción"
    )
    private val hindi = LanguageStrings(
        "हिंदी", "समायोजन", "अवलोकन", "लेन-देन", "हिसाब किताब", "श्रेणियाँ",
        "के बारे में", "नेविगेशन दराज खोलें", "वापस नेविगेट करें", "नया लेन-देन"
    )
    private val japanese = LanguageStrings(
        "日本語", "設定", "概要概要", "トランザクション", "アカウント",
        "カテゴリー","約", "ナビゲーションドロワーを開く", "戻る", "新規取引"
    )
    private val korean = LanguageStrings(
        "한국어", "설정", "개요", "트랜잭션", "계정", "카테고리", "약", "탐색 창 열기", "뒤로 탐색", "새로운 거래"
    )
    private val thai = LanguageStrings(
        "ไทย", "การตั้งค่า", "ภาพรวม", "การทำธุรกรรม", "บัญชี", "หมวดหมู่",
        "เกี่ยวกับ", "เปิด Navigation Drawer", "นำทางย้อนกลับ", "ธุรกรรมใหม่"
    )
}

/**
 *  Data class holding translated Screen strings and the [language] itself.
 */
data class LanguageStrings(
    val language: String,
    val settingsTitle: String,
    val overviewTitle: String,
    val tranTitle: String,
    val accTitle: String,
    val catTitle: String,
    val aboutTitle: String,
    val drawerContDesc: String,
    val navContDesc: String,
    val tranContDesc: String
)