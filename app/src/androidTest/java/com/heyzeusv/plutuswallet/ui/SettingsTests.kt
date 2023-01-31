package com.heyzeusv.plutuswallet.ui

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.preference.PreferenceManager
import com.heyzeusv.plutuswallet.R
import dagger.hilt.android.testing.HiltAndroidTest
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

@HiltAndroidTest
class SettingsTests : BaseTest() {

    // used to format strings
    private var setTotalFormatter = DecimalFormat()
    private var setDateFormatter = DateFormat.getDateInstance(0)

    // text color
    private var redId = R.color.colorExpenseTotal
    private var greenId = R.color.colorIncomeTotal

    private var currencySymbol = '$'
    private var currencySideLeft = true

    // LanguageTitles in each language available in app other than English
    private val german = LanguageTitles(
        "Deutsche", "die Einstellungen", "Überblick",
        "Transaktion", "Konten", "Kategorien", "Über"
    )
    private val spanish = LanguageTitles(
        "Español (latinoamericano)", "Configuraciones", "Visión general",
        "Transacción", "Cuentas", "Categorias", "Acerca de"
    )
    private val hindi = LanguageTitles(
        "हिंदी", "समायोजन", "अवलोकन",
        "लेन-देन", "हिसाब किताब", "श्रेणियाँ", "के बारे में"
    )
    private val japanese = LanguageTitles(
        "日本語", "設定", "概要概要",
        "トランザクション", "アカウント", "カテゴリー", "約"
    )
    private val korean = LanguageTitles(
        "한국어", "설정", "개요",
        "트랜잭션", "계정", "카테고리", "약"
    )
    private val thai = LanguageTitles(
        "ไทย", "การตั้งค่า", "ภาพรวม", "การทำธุรกรรม",
        "บัญชี", "หมวดหมู่", "เกี่ยวกับ"
    )

    @BeforeEach
    fun resetSharedPreferences() {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(composeRule.activity.baseContext)
        sharedPref.edit().clear().commit()
    }

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
    }

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
     *  Updates Decimal formatter using [decimal], [decimalSymbol], and [thousandsSymbol].
     */
    private fun updateDecimalFormatter(
        decimal: String,
        decimalSymbol: Char,
        thousandsSymbol: Char
    ) {

        val customSymbols = DecimalFormatSymbols(Locale.US)
        customSymbols.decimalSeparator = decimalSymbol
        customSymbols.groupingSeparator = thousandsSymbol
        setTotalFormatter = when (decimal) {
            "yes" -> DecimalFormat("#,##0.00", customSymbols)
            else -> DecimalFormat("#,##0", customSymbols)
        }
        setTotalFormatter.roundingMode = RoundingMode.HALF_UP
    }
}

/**
 *  Data class holding translated Fragment title strings and the [language] itself.
 */
data class LanguageTitles(
    val language: String,
    val settingsTitle: String,
    val cflTitle: String,
    val tranTitle: String,
    val accTitle: String,
    val catTitle: String,
    val aboutTitle: String
)