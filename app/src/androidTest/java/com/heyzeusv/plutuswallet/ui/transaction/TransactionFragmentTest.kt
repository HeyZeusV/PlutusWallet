package com.heyzeusv.plutuswallet.ui.transaction

import android.content.res.Resources
import android.os.Bundle
import android.widget.DatePicker
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.heyzeusv.plutuswallet.CustomMatchers.Companion.chipSelected
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@HiltAndroidTest
class TransactionFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repo: Repository

    val dd = DummyDataUtil()

    // used to get string resource
    private val resource: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Before
    fun init() {

        // populate @Inject fields in test class
        hiltRule.inject()
    }

    @Test
    fun displayBlankTransaction() {

        val expectedFormattedDate: String = DateFormat.getDateInstance(0).format(Date())

        // display a blank Transaction
        launchFragmentInHiltContainer<TransactionFragment>(Bundle(), R.style.AppTheme)

        // no details will be displayed, several views will not be displayed
        // Spinners will select first entry according to sorted DummyData
        onView(withId(R.id.tran_title)).check(matches(withText("")))
        onView(withId(R.id.tran_date)).check(matches(withText(expectedFormattedDate)))
        onView(withId(R.id.tran_account)).check(matches((withSpinnerText(dd.acc3.account))))
        onView(withId(R.id.tran_total)).check(matches(withText("")))
        onView(withId(R.id.tran_type_chips)).check(matches(chipSelected(R.id.tran_expense_chip)))
        onView(withId(R.id.tran_expense_cat)).check(matches(isDisplayed()))
        onView(withId(R.id.tran_expense_cat)).check(matches((withSpinnerText(dd.cat2.category))))
        onView(withId(R.id.tran_income_cat)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tran_memo)).check(matches(withText("")))
        onView(withId(R.id.tran_repeat)).check(matches(isNotChecked()))
        onView(withId(R.id.tran_period)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tran_frequency)).check(matches(not(isDisplayed())))
    }

    @Test
    fun displayGivenExpenseTransaction() {

        val expectedFormattedDate: String = DateFormat.getDateInstance(0).format(Date(86400000))

        // load Transaction with id 1, will be tran1 from DummyData
        val bundle = TransactionFragmentArgs(1).toBundle()

        // display Transaction
        launchFragmentInHiltContainer<TransactionFragment>(bundle, R.style.AppTheme)

        // Transaction details will be displayed
        onView(withId(R.id.tran_title)).check(matches(withText("Party")))
        onView(withId(R.id.tran_date)).check(matches(withText(expectedFormattedDate)))
        onView(withId(R.id.tran_account)).check(matches((withSpinnerText(dd.acc3.account))))
        onView(withId(R.id.tran_total)).check(matches(withText("1,000.10")))
        onView(withId(R.id.tran_type_chips)).check(matches(chipSelected(R.id.tran_expense_chip)))
        onView(withId(R.id.tran_expense_cat)).check(matches(isDisplayed()))
        onView(withId(R.id.tran_expense_cat)).check(matches((withSpinnerText(dd.cat1.category))))
        // expense Transaction so income Spinner is not needed
        onView(withId(R.id.tran_income_cat)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tran_memo)).check(matches(withText("Catering for party")))
        onView(withId(R.id.tran_repeat)).check(matches(isChecked()))
        onView(withId(R.id.tran_period)).check(matches(isDisplayed()))
        onView(withId(R.id.tran_period)).check(matches(withSpinnerText("Days")))
        onView(withId(R.id.tran_frequency)).check(matches(isDisplayed()))
        onView(withId(R.id.tran_frequency)).check(matches(withText("1")))
    }

    @Test
    fun displayGivenIncomeTransaction() {

        // expected formatted Date button text
        val expectedFormattedDate: String = DateFormat.getDateInstance(0).format(Date(86400000 * 4))

        // load Transaction with id 3, will be tran3 from DummyData
        val bundle = TransactionFragmentArgs(3).toBundle()

        // display Transaction
        launchFragmentInHiltContainer<TransactionFragment>(bundle, R.style.AppTheme)

        // Transaction details will be displayed
        onView(withId(R.id.tran_title)).check(matches(withText("Pay Day")))
        onView(withId(R.id.tran_date)).check(matches(withText(expectedFormattedDate)))
        onView(withId(R.id.tran_account)).check(matches((withSpinnerText(dd.acc2.account))))
        onView(withId(R.id.tran_total)).check(matches(withText("2,000.32")))
        onView(withId(R.id.tran_type_chips)).check(matches(chipSelected(R.id.tran_income_chip)))
        // income Transaction so expense Spinner is not needed
        onView(withId(R.id.tran_expense_cat)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tran_income_cat)).check(matches(isDisplayed()))
        onView(withId(R.id.tran_income_cat)).check(matches((withSpinnerText(dd.cat3.category))))
        onView(withId(R.id.tran_memo)).check(matches(withText("Best day of the month!")))
        onView(withId(R.id.tran_repeat)).check(matches(isChecked()))
        onView(withId(R.id.tran_period)).check(matches(isDisplayed()))
        onView(withId(R.id.tran_period)).check(matches(withSpinnerText("Months")))
        onView(withId(R.id.tran_frequency)).check(matches(isDisplayed()))
        onView(withId(R.id.tran_frequency)).check(matches(withText("1")))
    }
    
    @Test
    fun createNewThroughSpinner() {

        // names for newly created items
        val testAcc = "Test Account"
        val testExCat = "Test Expense Category"
        val testInCat = "Test Income Category"

        // "Create New ..."
        val createAcc: String = resource.getString(R.string.account_create)
        val createCat: String = resource.getString(R.string.category_create)

        // display blank Transaction
        launchFragmentInHiltContainer<TransactionFragment>(Bundle(), R.style.AppTheme)

        // create new Account
        onView(withId(R.id.tran_account)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(createAcc))).perform(click())
        onView(withId(R.id.dialog_input)).perform(typeText(testAcc))
        onView(withId(android.R.id.button1)).perform(click())
        // check that it exists
        onView(withId(R.id.tran_account)).check(matches(withSpinnerText(testAcc)))

        // create new expense Category and check that it exists
        onView(withId(R.id.tran_expense_cat)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(createCat))).perform(click())
        onView(withId(R.id.dialog_input)).perform(typeText(testExCat))
        onView(withId(android.R.id.button1)).perform(click())
        // check that it exists
        onView(withId(R.id.tran_expense_cat)).check(matches(withSpinnerText(testExCat)))

        // create new income Category
        onView(withId(R.id.tran_income_chip)).perform(click())
        onView(withId(R.id.tran_income_cat)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(createCat))).perform(click())
        onView(withId(R.id.dialog_input)).perform(typeText(testInCat))
        onView(withId(android.R.id.button1)).perform(click())
        // check that it exists
        onView(withId(R.id.tran_income_cat)).check(matches(withSpinnerText(testInCat)))
    }

    @Test
    fun cancelNewThroughSpinner() {

        // adding additional income Category
        runBlockingTest {
            repo.insertCategory(Category(0, "Z Cat", "Income"))
        }

        // "Create New ..."
        val createAcc: String = resource.getString(R.string.account_create)
        val createCat: String = resource.getString(R.string.category_create)

        // display blank Transaction
        launchFragmentInHiltContainer<TransactionFragment>(Bundle(), R.style.AppTheme)

        // select non-first Spinner entry
        onView(withId(R.id.tran_account)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Unused"))).perform(click())
        // attempt to create new item, but cancel
        onView(withId(R.id.tran_account)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(createAcc))).perform(click())
        onView(withId(android.R.id.button2)).perform(click())
        // check that previously selected entry is now selected
        onView(withId(R.id.tran_account)).check(matches(withSpinnerText("Unused")))

        // select non-first Spinner entry
        onView(withId(R.id.tran_expense_cat)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Food"))).perform(click())
        // attempt to create new item, but cancel
        onView(withId(R.id.tran_expense_cat)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(createCat))).perform(click())
        onView(withId(android.R.id.button2)).perform(click())
        // check that previously selected entry is now selected
        onView(withId(R.id.tran_expense_cat)).check(matches(withSpinnerText("Food")))

        // make income Spinner appear
        onView(withId(R.id.tran_income_chip)).perform(click())
        // select non-first Spinner entry
        onView(withId(R.id.tran_income_cat)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Z Cat"))).perform(click())
        // attempt to create new item, but cancel
        onView(withId(R.id.tran_income_cat)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(createCat))).perform(click())
        onView(withId(android.R.id.button2)).perform(click())
        // check that previously selected entry is now selected
        onView(withId(R.id.tran_income_cat)).check(matches(withSpinnerText("Z Cat")))
    }

    @Test
    fun setDate() {

        // TODO: testable in different languages once Date is replaced
        val expectedFormattedDate = "Monday, January 18, 2021"

        // display blank Transaction
        launchFragmentInHiltContainer<TransactionFragment>(Bundle(), R.style.AppTheme)

        // change the date using DatePickerDialog
        onView(withId(R.id.tran_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button1)).perform(click())
        // check new Date is formatted correctly
        onView(withId(R.id.tran_date)).check(matches(withText(expectedFormattedDate)))
    }

    @Test
    fun cancelSetDate() {

        // TODO: testable in different languages once Date is replaced
        val expectedFormattedDate = "Monday, January 18, 2021"

        // display blank Transaction
        launchFragmentInHiltContainer<TransactionFragment>(Bundle(), R.style.AppTheme)

        // change the date using DatePickerDialog
        onView(withId(R.id.tran_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button1)).perform(click())
        // change the date using DatePickerDialog, but cancel instead
        onView(withId(R.id.tran_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2020, 1, 18))
        onView(withId(android.R.id.button2)).perform(click())
        // check previously saved Date is formatted correctly
        onView(withId(R.id.tran_date)).check(matches(withText(expectedFormattedDate)))
    }

    @Test
    fun saveTransactionSnackbar() {

        // display blank Transaction
        launchFragmentInHiltContainer<TransactionFragment>(Bundle(), R.style.AppTheme)

        // check that SnackBar displays when saving
        onView(withId(R.id.transaction_save)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.snackbar_saved)))
    }

    @Test
    fun saveTransactionNoTitle() {

        // calculate new id
        val totalTrans: Int = (repo as FakeRepository).tranList.size + 1

        // "Transaction"
        val tran = resource.getString(R.string.transaction_empty_title)

        // display blank Transaction
        launchFragmentInHiltContainer<TransactionFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.transaction_save)).perform(click())
        // check title is correctly entered after saving
        onView(withId(R.id.tran_title)).check(matches(withText("$tran$totalTrans")))
    }

    @Test
    fun positiveFutureTranDialog() {

        // load Transaction with id 1, will be tran1 from DummyData
        val bundle = TransactionFragmentArgs(1, false).toBundle()

        // display Transaction
        launchFragmentInHiltContainer<TransactionFragment>(bundle, R.style.AppTheme)

        // change Date of Transaction that has been repeated already and save
        onView(withId(R.id.tran_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.transaction_save)).perform(click())
        // accept warning
        onView(withId(android.R.id.button1)).perform(click())
        // change Date of Transaction again and save
        onView(withId(R.id.tran_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 17))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.transaction_save)).perform(click())
        // check that warning does not appear again
        onView(withText(R.string.alert_dialog_future_transaction_warning)).check(doesNotExist())
    }

    @Test
    fun negativeFutureTranDialog() {

        // load Transaction with id 1, will be tran1 from DummyData
        val bundle = TransactionFragmentArgs(1, false).toBundle()

        // display Transaction
        launchFragmentInHiltContainer<TransactionFragment>(bundle, R.style.AppTheme)

        // change Date of Transaction that has been repeated already and save
        onView(withId(R.id.tran_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 18))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.transaction_save)).perform(click())
        // decline warning
        onView(withId(android.R.id.button2)).perform(click())
        // save again and check warning does not appear
        onView(withId(R.id.transaction_save)).perform(click())
        onView(withText(R.string.alert_dialog_future_transaction_warning)).check(doesNotExist())
        // change Date of Transaction again and save
        onView(withId(R.id.tran_date)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(setDate(2021, 1, 17))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.transaction_save)).perform(click())
        // check that warning appears again
        onView(withText(R.string.alert_dialog_future_transaction_warning))
            .check(matches(isDisplayed()))
    }
}