package com.heyzeusv.plutuswallet.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.viewmodels.CategoriesViewModel
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator3

private const val TAG = "PWCategoriesFragment"

class CategoriesFragment : BaseFragment() {

    // views
    private lateinit var circleIndicator     : CircleIndicator3
    private lateinit var categoriesViewPager : ViewPager2

    // list used to hold lists of Categories
    private var categoryLists : MutableList<List<Category>> = mutableListOf(emptyList(), emptyList())

    // list used to hold list of Category names
    private var categoryNameLists : MutableList<MutableList<String>> = mutableListOf(mutableListOf(), mutableListOf())

    // list used to hold lists of unique Categories by type being used
    private var uniqueCategoryLists : MutableList<List<String>> = mutableListOf(emptyList(), emptyList())

    // used to tell which page of ViewPager2 to scroll to
    private var typeChanged : Int = 0

    private val categoriesViewModel : CategoriesViewModel by lazy {
        ViewModelProviders.of(this).get(CategoriesViewModel::class.java)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_categories, container, false)

        circleIndicator     = view.findViewById(R.id.categories_circle_indicator) as CircleIndicator3
        categoriesViewPager = view.findViewById(R.id.categories_view_pager      ) as ViewPager2

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoriesViewModel.expenseCategoriesLiveData.observe(this, Observer {

            categoryNameLists[0].clear()
            it.forEach { category : Category ->

                categoryNameLists[0].add(category.category)
            }
            categoryLists[0] = it
            updateUI(categoryLists)
        })

        categoriesViewModel.incomeCategoriesLiveData.observe(this, Observer {

            categoryNameLists[1].clear()
            it.forEach { category : Category ->

                categoryNameLists[1].add(category.category)
            }
            categoryLists[1] = it
            updateUI(categoryLists)
        })

        categoriesViewModel.uniqueExpenseLiveData.observe(this, Observer {

            uniqueCategoryLists[0] = it
            updateUI(categoryLists)
        })

        categoriesViewModel.uniqueIncomeLiveData.observe(this, Observer {

            uniqueCategoryLists[1] = it
            updateUI(categoryLists)
        })
    }

    /**
     *  Ensures the UI is up to date with correct information.
     *
     *  @param categoryLists list of lists to be shown in RecyclerViews.
     */
    private fun updateUI(categoryLists : MutableList<List<Category>>) {

        categoriesViewPager.adapter = CategoryListAdapter(categoryLists)
        categoriesViewPager.setCurrentItem(typeChanged, false)
        circleIndicator.setViewPager(categoriesViewPager)
    }

    /**
     *  Creates ViewHolder and binds ViewHolder to data from model layer.
     *
     *  @param categoryLists the list of lists of Categories.
     */
    private inner class CategoryListAdapter(var categoryLists : MutableList<List<Category>>)
        : RecyclerView.Adapter<CategoryListHolder>() {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListHolder {

            val view : View = layoutInflater.inflate(R.layout.item_view_category_list, parent, false)
            return CategoryListHolder(view)
        }

        override fun getItemCount() : Int = categoryLists.size

        // populates given holder with list of Category names from the given position in list
        override fun onBindViewHolder(holder: CategoryListHolder, position: Int) {

            val categoryList : List<Category> = categoryLists[position]
            holder.bind(categoryList, position)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view
     */
    private inner class CategoryListHolder(view : View) : RecyclerView.ViewHolder(view) {

        // views in ItemView
        private val categoryTypeTextView : TextView     = itemView.findViewById(R.id.categories_type           )
        private val categoryRecyclerView : RecyclerView = itemView.findViewById(R.id.categories_recycler_view)

        fun bind(categoryList : List<Category>, type : Int) {

            if (type == 0) {

                categoryTypeTextView.text = getString(R.string.type_expense)
            } else {

                categoryTypeTextView.text = getString(R.string.type_income)
            }
            val linearLayoutManager = LinearLayoutManager(context)
            categoryRecyclerView.layoutManager = linearLayoutManager
            categoryRecyclerView.adapter = CategoryAdapter(categoryList)
            categoryRecyclerView.addItemDecoration(
                    DividerItemDecoration(categoryRecyclerView.context, DividerItemDecoration.VERTICAL))
        }

        /**
         *  Creates ViewHolder and binds ViewHolder to data from model layer.
         *
         *  @param categoryList the list of Categories.
         */
        private inner class CategoryAdapter(var categoryList : List<Category>)
            : RecyclerView.Adapter<CategoryHolder>() {

            // creates view to display, wraps the view in a ViewHolder and returns the result
            override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : CategoryHolder {

                val view : View = layoutInflater.inflate(R.layout.item_view_category, parent, false)
                return CategoryHolder(view)
            }

            override fun getItemCount() : Int = categoryList.size

            // populates given holder with Category from the given position in CategoryList
            override fun onBindViewHolder(holder : CategoryHolder, position : Int) {

                val category : Category = categoryList[position]
                holder.bind(category)
            }
        }

        /**
         *  ViewHolder stores a reference to an item's view
         */
        private inner class CategoryHolder(view : View)
            : RecyclerView.ViewHolder(view) {

            // views in ItemView
            private val editButton       : MaterialButton = itemView.findViewById(R.id.category_edit  )
            private val deleteButton     : MaterialButton = itemView.findViewById(R.id.category_delete)
            private val categoryTextView : TextView       = itemView.findViewById(R.id.category_name  )

            fun bind(category : Category) {

                val type : Int = when (category.type) {

                    "Expense" -> 0
                    else      -> 1
                }

                categoryTextView.text = category.category

                if (!uniqueCategoryLists[type].contains(category.category)) {

                    deleteButton.isEnabled = true

                    deleteButton.setOnClickListener {

                        launch {

                            categoriesViewModel.deleteCategory(category)
                        }
                        typeChanged = type
                    }
                }

                editButton.setOnClickListener {

                    // initialize instance of Builder
                    val builder : MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                        // set title of AlertDialog
                        .setTitle(getString(R.string.category_create))
                    // inflates view that holds EditText
                    val viewInflated : View = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_new_category, view as ViewGroup, false)
                    // the EditText to be used
                    val input : EditText = viewInflated.findViewById(R.id.category_Input)
                    // sets the view
                    builder.setView(viewInflated)
                        // set positive button and its click listener
                        .setPositiveButton(getString(R.string.alert_dialog_save)) { _ : DialogInterface, _ : Int ->

                            editCategory(input.text.toString(), category, type)
                            typeChanged = type
                        }
                        // set negative button and its click listener
                        .setNegativeButton(getString(R.string.alert_dialog_cancel)) { _ : DialogInterface, _ : Int ->


                        }
                    // make the AlertDialog using the builder
                    val categoryAlertDialog : AlertDialog = builder.create()
                    // display AlertDialog
                    categoryAlertDialog.show()
                }
            }

            private fun editCategory(updatedName : String, category : Category, type : Int) {

                if (categoryNameLists[type].contains(updatedName)) {

                    val existBar : Snackbar = Snackbar.make(view!!, "$updatedName already exists!", Snackbar.LENGTH_SHORT)
                    existBar.anchorView = circleIndicator
                    existBar.show()
                } else {

                    category.category = updatedName
                    launch {

                        categoriesViewModel.updateCategory(category)
                    }
                }
            }
        }
    }

    companion object {

        /**
         *  Initializes instance of CategoriesFragment
         */
        fun newInstance() : CategoriesFragment {

            return CategoriesFragment()
        }
    }
}