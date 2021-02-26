package com.heyzeusv.plutuswallet.ui.category

import com.heyzeusv.plutuswallet.InstantExecutorExtension
import com.heyzeusv.plutuswallet.TestCoroutineExtension
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, TestCoroutineExtension::class)
internal class CategoryViewModelTest {

    // test Fake
    private val repo = FakeRepository()

    // what is being tested
    private lateinit var catVM: CategoryViewModel

    // dummy data
    private val dd = DummyDataUtil()

    @BeforeEach
    fun setUpViewModel() {

        // reset fake repo with dummy data and pass it to ViewModel
        repo.resetLists()
        catVM = CategoryViewModel(repo)
    }

    @Test
    @DisplayName("Should create edit Event containing Category when edit button is pressed")
    fun editCategoryOC() {

        val cat = Category(0, "Test", "Expense")

        catVM.editCategoryOC(cat)
        val value: Event<Category> = catVM.editCategoryEvent.value!!

        assertEquals(value.getContentIfNotHandled(), cat)
    }

    @Test
    @DisplayName("Should create delete Event containing Category when delete button is pressed")
    fun deleteCategoryOC() {

        val cat = Category(0, "Test", "Expense")

        catVM.deleteCategoryOC(cat)
        val value: Event<Category> = catVM.deleteCategoryEvent.value!!

        assertEquals(value.getContentIfNotHandled(), cat)
    }

    @Test
    @DisplayName("Should initialize type lists containing all Category names and Categories being used")
    fun initNamesUsedLists() {

        val expectedExNames: MutableList<String> = mutableListOf("Entertainment", "Food", "Unused Expense")
        val expectedExUsed: MutableList<String> = mutableListOf("Entertainment", "Food")
        val expectedInNames: MutableList<String> = mutableListOf("Salary", "Unused Income", "Zelle")
        val expectedInUsed: MutableList<String> = mutableListOf("Salary")

        runBlocking {
            catVM.initNamesUsedLists("Expense", 0)
            catVM.initNamesUsedLists("Income", 1)
        }

        assertEquals(expectedExNames, catVM.catNames[0])
        assertEquals(expectedExUsed, catVM.catsUsed[0])
        assertEquals(expectedInNames, catVM.catNames[1])
        assertEquals(expectedInUsed, catVM.catsUsed[1])
    }

    @Test
    @DisplayName("Should delete Category of type from lists and database")
    fun deleteCategoryPosFun() {

        val expectedExNames: MutableList<String> = mutableListOf("Entertainment", "Unused Expense")
        val expectedExUsed: MutableList<String> = mutableListOf("Entertainment")
        val expectedInNames: MutableList<String> = mutableListOf("Unused Income", "Zelle")
        val expectedInUsed: MutableList<String> = mutableListOf()
        catVM.catNames[0] = mutableListOf("Food", "Entertainment", "Unused Expense")
        catVM.catsUsed[0] = mutableListOf("Food", "Entertainment")
        catVM.catNames[1] = mutableListOf("Salary", "Unused Income", "Zelle")
        catVM.catsUsed[1] = mutableListOf("Salary")

        catVM.deleteCategoryPosFun(dd.cat1, 0)
        catVM.deleteCategoryPosFun(dd.cat3, 1)

        assertEquals(expectedExNames, catVM.catNames[0])
        assertEquals(expectedExUsed, catVM.catsUsed[0])
        assertEquals(expectedInNames, catVM.catNames[1])
        assertEquals(expectedInUsed, catVM.catsUsed[1])
        assertEquals(mutableListOf(dd.cat2, dd.cat4, dd.cat5, dd.cat6), repo.catList)
    }

    @Test
    @DisplayName("Should edit Category of type with a new name")
    fun editCategoryName() {

        val expectedExNames: MutableList<String> = mutableListOf("Entertainment", "Test1")
        val expectedExUsed: MutableList<String> = mutableListOf("Entertainment", "Test1")
        val expectedInNames: MutableList<String> = mutableListOf("Test2")
        val expectedInUsed: MutableList<String> = mutableListOf("Test2")
        catVM.catNames[0] = mutableListOf("Entertainment", "Food")
        catVM.catsUsed[0] = mutableListOf("Entertainment", "Food")
        catVM.catNames[1] = mutableListOf("Salary")
        catVM.catsUsed[1] = mutableListOf("Salary")

        catVM.editCategoryName(dd.cat1, "Test1", 0)
        catVM.editCategoryName(dd.cat3, "Test2", 1)

        assertEquals(expectedExNames, catVM.catNames[0])
        assertEquals(expectedExUsed, catVM.catsUsed[0])
        assertEquals(expectedInNames, catVM.catNames[1])
        assertEquals(expectedInUsed, catVM.catsUsed[1])
        assertEquals(Category(1, "Test1", "Expense"), repo.catList[0])
        assertEquals(Category(3, "Test2", "Income"), repo.catList[2])
    }

    @Test
    @DisplayName("Should edit Category of type with an existing name which creates and exists Event")
    fun editCategoryNameExists() {

        catVM.catNames[0] = mutableListOf("Entertainment", "Food")

        catVM.editCategoryName(dd.cat2, "Food", 0)
        val value: Event<String> = catVM.existsCategoryEvent.value!!

        assertEquals(value.getContentIfNotHandled(), "Food")
    }

    @Test
    @DisplayName("Should insert a new unique Category of type")
    fun insertNewCategory() {

        val newExCategory = Category(4, "Test1", "Expense")
        val newInCategory = Category(5, "Test2", "Income")

        catVM.insertNewCategory(newExCategory, "Test1", 0)
        catVM.insertNewCategory(newInCategory, "Test2", 1)

        assert(repo.catList.contains(newExCategory))
        assert(repo.catList.contains(newInCategory))
        assertEquals(mutableListOf("Test1"), catVM.catNames[0])
        assertEquals(mutableListOf("Test2"), catVM.catNames[1])
    }

    @Test
    @DisplayName("Should insert a Category with an existing name which creates an exist Event")
    fun insertNewCategoryExists() {

        val newCategory = Category(100, "Salary", "Income")
        catVM.catNames[1] = mutableListOf("Salary")

        catVM.insertNewCategory(newCategory, "Salary", 1)
        val value: Event<String> = catVM.existsCategoryEvent.value!!

        assertEquals(value.getContentIfNotHandled(), "Salary")
    }
}