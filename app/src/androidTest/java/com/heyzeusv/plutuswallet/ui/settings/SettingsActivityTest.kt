package com.heyzeusv.plutuswallet.ui.settings

import android.view.Gravity
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withResourceName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.rvViewHolder
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.withIndex
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.withTextColor
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.DummyAndroidDataUtil
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
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
 *  Test device should have default language of English!
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class SettingsActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    val dd = DummyAndroidDataUtil()

    private var totalFormatter = DecimalFormat()
    private var dateFormatter = DateFormat.getDateInstance()

    private var redId = android.R.color.holo_red_dark
    private var greenId = android.R.color.holo_green_dark

    private var currencySymbol = '$'
    private var currencySideLeft = true

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
        updateFormatters(true,'.', ',', 0)

        // format date according to default Date format
        val formattedTranDate = dateFormatter.format(dd.tran3.date)

        /**
         *  Checks that the various default Settings are correctly display throughout app.
         *  '$' on left side, ',' used as thousands symbols, '.' used as decimal symbol,
         *  decimals allowed, full date is displayed, and English is language.
         */
        // checks that the various default Settings are correctly display throughout app
        // "$" on left side; "," as thousands symbol;
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
        onView(withId(R.id.symbolLeftTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.symbolLeftTextView)).check(matches(withText("$")))
        onView(withId(R.id.tran_total)).check(matches(withText("2,000.32")))
        onView(withId(R.id.symbolRightTextView)).check(matches(not(isDisplayed())))
        Espresso.pressBack()
        navigateToFragAndCheckTitle(R.id.accountFragment, R.id.account_topBar, "Accounts")
        navigateToFragAndCheckTitle(R.id.categoryFragment, R.id.category_topBar, "Categories")
        navigateToFragAndCheckTitle(R.id.aboutFragment, R.id.about_topBar, "About")
        navigateToSettingsAndCheckTitle("Settings")
    }

    @Test
    fun changeSettingsRegardingTotal() {

        navigateToSettingsAndCheckTitle("Settings")
        clickOnPreference(R.string.preferences_currency_symbol)
        onView(withText("€")).perform(click())
        clickOnPreference(R.string.preferences_symbol_side)
        clickOnPreference(R.string.preferences_decimal_symbol)
        onView(withText("\"-\"")).perform(click())
        clickOnPreference(R.string.preferences_thousands_symbol)
        onView(withText("\".\"")).perform(click())

        updateCurrency('€', false)
        updateFormatters(true, '-', '.', 0)

        Espresso.pressBack()

        Thread.sleep(3000)

        onView(withIndex(withId(R.id.ivt_layout), 2)).perform(click())
        Thread.sleep(3000)
        onView(withId(R.id.symbolLeftTextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tran_total)).check(matches(withText("2.000-32")))
        onView(withId(R.id.symbolRightTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.symbolRightTextView)).check(matches(withText("€")))

        Thread.sleep(4000)
    }

    private fun clickOnPreference(prefName: Int) {

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(prefName)), click()))
    }

    private fun updateCurrency(symbol: Char, symbolLeft: Boolean) {

        currencySymbol = symbol
        currencySideLeft = symbolLeft
    }

    private fun updateFormatters(
        decimal: Boolean,
        decimalSymbol: Char,
        thousandsSymbol: Char,
        dateStyle: Int) {

        val customSymbols = DecimalFormatSymbols(Locale.US)
        customSymbols.decimalSeparator = decimalSymbol
        customSymbols.groupingSeparator = thousandsSymbol
        totalFormatter = when (decimal) {
            true -> DecimalFormat("#,##0.00", customSymbols)
            else -> DecimalFormat("#,##0", customSymbols)
        }
        totalFormatter.roundingMode = RoundingMode.HALF_UP

        dateFormatter = DateFormat.getDateInstance(dateStyle)
    }
    /**
     *  Checks that ViewHolder at [pos] in RecyclerView is displaying the correct [tran] details,
     *  as well as the [color] of Total text.
     */
    private fun checkTranViewHolder(
        pos: Int,
        tran: Transaction,
        color: Int,
    ) {

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

    private fun navigateToSettingsAndCheckTitle(title: String) {

        // opens drawer and navigates to SettingsFragment
        onView(withId(R.id.activity_drawer))
            .check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open())
        onView(withId(R.id.activity_nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.actionSettings))
        // check to make sure it did navigate to SettingsFragment
        onView(allOf(instanceOf(TextView::class.java),
            withParent(withResourceName("action_bar"))))
            .check(matches(withText(title)))
    }

    private fun navigateToFragAndCheckTitle(actionId: Int, topBarId: Int, title: String) {

        onView(withId(R.id.activity_drawer))
            .check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open())
        onView(withId(R.id.activity_nav_view))
            .perform(NavigationViewActions.navigateTo(actionId))
        onView(allOf(instanceOf(TextView::class.java),
            withParent(withId(topBarId))))
            .check(matches(withText(title)))
        Espresso.pressBack()
    }
}