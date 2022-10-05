package com.heyzeusv.plutuswallet.ui.settings

import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions.navigateTo
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withResourceName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.rvViewHolder
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.withIndex
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.withPrefix
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.withSuffix
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.withTextColor
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 *  All tests start on [MainActivity] since starting on [SettingsActivity] and pressing back
 *  will close the app rather than going to [MainActivity].
 *  Test device should have default language of English!
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class SettingsActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    val dd = DummyDataUtil()

    // used to format strings
    private var totalFormatter = DecimalFormat()
    private var dateFormatter = DateFormat.getDateInstance(0)

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

    @Before
    fun init() {

        // populate @Inject fields in test class
        hiltRule.inject()

        // start scenario before every test
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun close() {

        // close scenario after every test
        activityScenario.close()
    }

    @Test
    fun displaySettings() {

        // quick test to make sure navigating to Settings works
        navigateToSettingsAndCheckTitle("Settings")
    }

    @Test
    fun checkInitialSettings() {

        // default SettingsValues
        updateDecimalFormatter(true, '.', ',')

        // format date according to default Date format
        val formattedTranDate = dateFormatter.format(dd.tran3.date)

        /**
         *  Checks that the various default Settings are correctly display throughout app.
         *  '$' on left side, ',' used as thousands symbols, '.' used as decimal symbol,
         *  decimals allowed, full date is displayed, and English is language.
         */
        // check that app follows system setting
        assert(AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(R.id.cfl_topBar))))
            .check(matches(withText("Overview")))
        checkTranViewHolder(0, dd.tran1, redId)
        checkTranViewHolder(1, dd.tran2, redId)
        checkTranViewHolder(2, dd.tran3, greenId)
        checkTranViewHolder(3, dd.tran4, redId)
        onView(withIndex(withId(R.id.ivt_layout), 2)).perform(click())
        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(R.id.tran_topBar))))
            .check(matches(withText("Transaction")))
        onView(withId(R.id.tran_date)).check(matches(withText(formattedTranDate)))
        onView(withId(R.id.tran_total_layout)).check(matches(withPrefix("$")))
        onView(withId(R.id.tran_total)).check(matches(withText("2,000.32")))
        onView(withId(R.id.tran_total_layout)).check(matches(withSuffix(null)))
        pressBack()
        navigateToFragAndCheckTitle(R.id.accountFragment, R.id.account_topBar, "Accounts")
        navigateToFragAndCheckTitle(R.id.categoryFragment, R.id.category_topBar, "Categories")
        navigateToFragAndCheckTitle(R.id.aboutFragment, R.id.about_topBar, "About")
        navigateToSettingsAndCheckTitle("Settings")
    }

    @Test
    fun changeTheme() {

        // check Light mode
        assert(changeThemeAndGetUiMode(R.string.preferences_theme_light)
                == AppCompatDelegate.MODE_NIGHT_NO)

        // check Dark mode
        assert(changeThemeAndGetUiMode(R.string.preferences_theme_dark)
                == AppCompatDelegate.MODE_NIGHT_YES)

        // check that app follows system setting
        assert(changeThemeAndGetUiMode(R.string.preferences_theme_system)
                == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    @Test
    fun changeSettingsRegardingTotal() {

        // symbols that can be changed
        var (currency, decimal, thousands) = arrayOf("€", "\"-\"", "\".\"")

        // used so parameter name wouldn't be needed for using Boolean literals
        val symbolLeft = true
        val decimalPlace = true

        // navigate to Settings, change symbols, then check ViewHolders and Transaction
        navigateToSettingsAndCheckTitle("Settings")
        clickOnPreference(R.string.preferences_symbol_side)
        changeSymbols(currency, decimal, thousands, !symbolLeft, decimalPlace)
        checkViewHoldersAndTranSymbol(!symbolLeft, currency, "2.000-32")

        // change values, navigate to Settings, change symbols, then check ViewHolders and Transaction
        currency = "£"; decimal = "\"\u0020\""; thousands = "\"-\""
        navigateToSettingsAndCheckTitle("Settings")
        clickOnPreference(R.string.preferences_symbol_side)
        changeSymbols(currency, decimal, thousands, symbolLeft, decimalPlace)
        checkViewHoldersAndTranSymbol(symbolLeft, currency, "2-000 32")

        // change values, navigate to Settings, change symbols, then check ViewHolders and Transaction
        currency = "¥"; decimal = "\",\""; thousands = "\"\u0020\""
        navigateToSettingsAndCheckTitle("Settings")
        changeSymbols(currency, decimal, thousands, symbolLeft, decimalPlace)
        checkViewHoldersAndTranSymbol(symbolLeft, currency, "2 000,32")

        // change values, navigate to Settings, change symbols, then check ViewHolders and Transaction
        currency = "₹"; decimal = "\".\""; thousands = "\",\""
        navigateToSettingsAndCheckTitle("Settings")
        changeSymbols(currency, decimal, thousands, symbolLeft, decimalPlace)
        checkViewHoldersAndTranSymbol(symbolLeft, currency, "2,000.32")

        // change values, navigate to Settings, change symbols, then check ViewHolders and Transaction
        currency = "₩"
        navigateToSettingsAndCheckTitle("Settings")
        clickOnPreference(R.string.preferences_number_decimal)
        onView(withId(android.R.id.button1)).perform(click())
        changeSymbols(currency, decimal, thousands, symbolLeft, !decimalPlace)
        checkViewHoldersAndTranSymbol(symbolLeft, currency, "2,000")

        // change values, navigate to Settings, change symbols, then check ViewHolders and Transaction
        currency = "฿"
        navigateToSettingsAndCheckTitle("Settings")
        changeSymbols(currency, decimal, thousands, symbolLeft, !decimalPlace)
        checkViewHoldersAndTranSymbol(symbolLeft, currency, "2,000")

        // return to original settings
        currency = "$"
        navigateToSettingsAndCheckTitle("Settings")
        clickOnPreference(R.string.preferences_number_decimal)
        onView(withId(android.R.id.button1)).perform(click())
        changeSymbols(currency, decimal, thousands, symbolLeft, decimalPlace)
    }

    @Test
    fun changeDateFormat() {

        // set Decimal formatter to use default values
        updateDecimalFormatter(true, '.', ',')

        // change Date Format and check ViewHolders and Transaction button text
        changeDateFormatAndCheckViewHoldersAndTran("April 19, 1993", 1)
        changeDateFormatAndCheckViewHoldersAndTran("Apr 19, 1993", 2)
        changeDateFormatAndCheckViewHoldersAndTran("4/19/93", 3)

        // return to original settings
        navigateToSettingsAndCheckTitle("Settings")
        clickOnPreference(R.string.preferences_date_format)
        onView(withText("Monday, April 19, 1993")).perform(click())
        dateFormatter = DateFormat.getDateInstance(0)
    }

    @Test
    fun changeLanguage() {

        // navigate to SettingsFragment and check that app is in English
        navigateToSettingsAndCheckTitle("Settings")
        // change language and check titles of all Fragments in app
        changeLanguageAndCheckTitles(german)
        changeLanguageAndCheckTitles(spanish)
        changeLanguageAndCheckTitles(hindi)
        changeLanguageAndCheckTitles(japanese)
        changeLanguageAndCheckTitles(korean)
        changeLanguageAndCheckTitles(thai)

        // return to original settings
        clickOnPreference(R.string.preferences_language)
        onView(withText("English")).perform(click())
    }

    @Test
    fun swapThousandsDecimalSymbols() {

        // navigate to Settings and select the same decimal symbol used for thousands
        // accept the pop-up which causes the 2 settings to swap symbols
        navigateToSettingsAndCheckTitle("Settings")
        clickOnPreference(R.string.preferences_thousands_symbol)
        onView(withText("\".\"")).perform(click())
        onView(withId(android.R.id.button1)).perform(click())
        updateDecimalFormatter(true, ',', '.')

        // check ViewHolders and Transaction
        checkViewHoldersAndTranSymbol(true, "$", "2.000,32")

        // reset settings
        navigateToSettingsAndCheckTitle("Settings")
        clickOnPreference(R.string.preferences_decimal_symbol)
        onView(withText("\".\"")).perform(click())
        onView(withId(android.R.id.button1)).perform(click())
        updateDecimalFormatter(true, '.', ',')
    }

    /**
     *  Navigates to SettingsFragment, clicks on Theme preference and sets it to [theme] and
     *  navigates back to CFLFragment.
     */
    private fun changeThemeAndGetUiMode(theme: Int): Int {

        navigateToSettingsAndCheckTitle("Settings")
        clickOnPreference(R.string.preferences_theme)
        onView(withText(theme)).perform(click())
        pressBack()

        return AppCompatDelegate.getDefaultNightMode()
    }

    /**
     *  Helper function for changeSettingsRegardingTotal() test. Changes symbol settings according
     *  to [currency], [decimal], and [thousands]. Updates currency variables and formatters
     *  using above values in addition to [symbolLeft] and [decimalPlace].
     */
    private fun changeSymbols(
        currency: String,
        decimal: String,
        thousands: String,
        symbolLeft: Boolean,
        decimalPlace: Boolean
    ) {

        // converts string to char
        val decimalChar = when (decimal) {
            "\",\"" -> ','
            "\".\"" -> '.'
            "\"-\"" -> '-'
            else -> ' '
        }
        val thousandsChar = when (thousands) {
            "\",\"" -> ','
            "\".\"" -> '.'
            "\"-\"" -> '-'
            else -> ' '
        }

        // change settings
        clickOnPreference(R.string.preferences_currency_symbol)
        onView(withText(currency)).perform(click())
        if (decimalPlace) {
            clickOnPreference(R.string.preferences_decimal_symbol)
            onView(withText(decimal)).perform(click())
        }
        clickOnPreference(R.string.preferences_thousands_symbol)
        onView(withText(thousands)).perform(click())

        // update variables and formatters
        updateCurrency(currency.single(), symbolLeft)
        updateDecimalFormatter(decimalPlace, decimalChar, thousandsChar)
    }

    /**
     *  Clicks on Preference with given [prefId]
     */
    private fun clickOnPreference(prefId: Int) {

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(prefId)), click()))
    }

    /**
     *  Updates currency Global variables with [symbol] and [symbolLeft].
     */
    private fun updateCurrency(symbol: Char, symbolLeft: Boolean) {

        currencySymbol = symbol
        currencySideLeft = symbolLeft
    }

    /**
     *  Updates Decimal formatter using [decimal], [decimalSymbol], and [thousandsSymbol].
     */
    private fun updateDecimalFormatter(
        decimal: Boolean,
        decimalSymbol: Char,
        thousandsSymbol: Char
    ) {

        val customSymbols = DecimalFormatSymbols(Locale.US)
        customSymbols.decimalSeparator = decimalSymbol
        customSymbols.groupingSeparator = thousandsSymbol
        totalFormatter = when (decimal) {
            true -> DecimalFormat("#,##0.00", customSymbols)
            else -> DecimalFormat("#,##0", customSymbols)
        }
        totalFormatter.roundingMode = RoundingMode.HALF_UP
    }

    /**
     *  Checks that ViewHolder at [pos] in RecyclerView is displaying the correct [tran] details,
     *  as well as the [color] of Total text.
     */
    private fun checkTranViewHolder(pos: Int, tran: Transaction, color: Int) {

        // Date to be shown in ItemView
        val formattedDate: String = dateFormatter.format(tran.date)
        val formattedTotal = when (currencySideLeft) {
            true -> "$currencySymbol${totalFormatter.format(tran.total)}"
            else -> "${totalFormatter.format(tran.total)}$currencySymbol"
        }

        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withText(tran.title), R.id.ivt_title)))
        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withText(tran.account), R.id.ivt_account)))
        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withText(formattedDate), R.id.ivt_date)))
        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withText(formattedTotal), R.id.ivt_total)))
        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withTextColor(color), R.id.ivt_total)))
        onView(withId(R.id.tranlist_rv))
            .check(matches(rvViewHolder(pos, withText(tran.category), R.id.ivt_category)))
    }

    /**
     *  Checks that all ViewHolders are displaying correct information.
     */
    private fun checkAllTranViewHolders() {

        checkTranViewHolder(0, dd.tran1, redId)
        checkTranViewHolder(1, dd.tran2, redId)
        checkTranViewHolder(2, dd.tran3, greenId)
        checkTranViewHolder(3, dd.tran4, redId)
    }

    /**
     *  Navigates to SettingFragment from CFLFragment and checks that title in TopBar is [title].
     */
    private fun navigateToSettingsAndCheckTitle(title: String) {

        // opens drawer and navigates to SettingsFragment
        onView(withId(R.id.activity_drawer))
            .check(matches(isClosed(Gravity.LEFT))).perform(open())
        onView(withId(R.id.activity_nav_view))
            .perform(navigateTo(R.id.actionSettings))
        // check to make sure it did navigate to SettingsFragment
        onView(allOf(instanceOf(TextView::class.java),
            withParent(withResourceName("action_bar"))))
            .check(matches(withText(title)))
    }

    /**
     *  Navigates to fragment in Drawer using [actionId] from CFLFragment.
     *  Uses [topBarId] to check that TopBar has title of [title].
     */
    private fun navigateToFragAndCheckTitle(actionId: Int, topBarId: Int, title: String) {

        // open drawer and select item with actionId and check title
        onView(withId(R.id.activity_drawer))
            .check(matches(isClosed(Gravity.LEFT))).perform(open())
        onView(withId(R.id.activity_nav_view))
            .perform(navigateTo(actionId))
        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(topBarId))))
            .check(matches(withText(title)))
        // hate to do this, but AboutFragment needs a slight delay before pressing back works
        // correctly, especially in changeLanguage() test
        if (topBarId == R.id.about_topBar) Thread.sleep(500)
        pressBack()
    }

    /**
     *  Navigates to all Fragments within app other than SettingsFragment and checks their title
     *  to ensure it matches correctly with provided titles.
     */
    private fun navigateToFragsAndCheckTitles(
        cflTitle: String,
        tranTitle: String,
        accTitle: String,
        catTitle: String,
        aboutTitle: String,
    ) {

        // checks CFLFragment titles
        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(R.id.cfl_topBar))))
            .check(matches(withText(cflTitle)))

        // navigates to TransactionFragment and checks title
        onView(withId(R.id.cfl_new_tran)).perform(click())
        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(R.id.tran_topBar))))
            .check(matches(withText(tranTitle)))
        pressBack()

        // navigates to other fragments using above function and checks title
        navigateToFragAndCheckTitle(R.id.accountFragment, R.id.account_topBar, accTitle)
        navigateToFragAndCheckTitle(R.id.categoryFragment, R.id.category_topBar, catTitle)
        navigateToFragAndCheckTitle(R.id.aboutFragment, R.id.about_topBar, aboutTitle)
    }

    /**
     *  Helper function for changeSettingsRegardingTotal() test. Leaves SettingsFragment, then
     *  checks ViewHolders, then navigates to TransactionFragment to check if [symbolLeft] side
     *  with [currency] symbol, and if [total] has correct symbols. Returns to CFLFragment.
     */
    private fun checkViewHoldersAndTranSymbol(symbolLeft: Boolean, currency: String, total: String) {

        // return from settings and close menu
        pressBack()
        pressBack()
        checkAllTranViewHolders()
        checkTransactionSymbols(symbolLeft, currency, total)
        pressBack()
    }

    /**
     *  Clicks on third entry in TransactionList. Checks that [total] has correct symbols.
     *  Checks that correct views are visible depending on [symbolLeft]
     *  and with correct [currency] symbol.
     */
    private fun checkTransactionSymbols(symbolLeft: Boolean, currency: String, total: String) {

        onView(withIndex(withId(R.id.ivt_layout), 2)).perform(click())
        onView(withId(R.id.tran_total)).check(matches(withText(total)))
        if (symbolLeft) {
            onView(withId(R.id.tran_total_layout)).check(matches(withPrefix(currency)))
            onView(withId(R.id.tran_total_layout)).check(matches(withSuffix(null)))
        } else {
            onView(withId(R.id.tran_total_layout)).check(matches(withPrefix(null)))
            onView(withId(R.id.tran_total_layout)).check(matches(withSuffix(currency)))
        }
    }

    /**
     *  Helper function for changeDateFormat() test. Navigates to SettingsFragment and change
     *  DateFormat setting to [dateFormat] style with [dateStyle] representing its code in
     *  [DateFormat] class.
     */
    private fun changeDateFormatAndCheckViewHoldersAndTran(dateFormat: String, dateStyle: Int) {

        // navigate to SettingsFragment and check Date Format setting
        navigateToSettingsAndCheckTitle("Settings")
        clickOnPreference(R.string.preferences_date_format)
        onView(withText(dateFormat)).perform(click())
        // update DateFormatter
        dateFormatter = DateFormat.getDateInstance(dateStyle)

        // navigate back to CFLFragment and close menu
        pressBack()
        pressBack()
        // check all ViewHolder display correct info
        checkAllTranViewHolders()

        // navigate to TransactionFragment and check Date button text
        onView(withIndex(withId(R.id.ivt_layout), 2)).perform(click())
        onView(withId(R.id.tran_date)).check(matches(withText(dateFormatter.format(dd.tran3.date))))
        // navigate back to CFLFragment
        pressBack()
    }

    /**
     *  Helper function for changeLanguage() test. Changes selected language then goes through
     *  all Fragments within app and checks their titles using [language].
     */
    private fun changeLanguageAndCheckTitles(language: LanguageTitles) {

        // change language setting
        clickOnPreference(R.string.preferences_language)
        onView(withText(language.language)).perform(click())
        pressBack()

        // check each fragment and their titles
        navigateToFragsAndCheckTitles(
            language.cflTitle, language.tranTitle, language.accTitle,
            language.catTitle, language.aboutTitle
        )
        // navigate back to settings and check title
        navigateToSettingsAndCheckTitle(language.settingsTitle)
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