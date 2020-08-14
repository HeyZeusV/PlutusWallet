package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.viewmodels.CategoryViewModel
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator3

/**
 *  Shows all Categories depending on type in database and allows users to either
 *  edit them or delete them.
 */
class CategoryFragment : BaseFragment() {

    // views
    private lateinit var circleIndicator     : CircleIndicator3
    private lateinit var categoriesViewPager : ViewPager2

    // list used to hold lists of Categories
    private var categoryLists : MutableList<List<Category>>
            = mutableListOf(emptyList(), emptyList())

    // list used to hold list of Category names
    private var categoryNameLists : MutableList<MutableList<String>>
            = mutableListOf(mutableListOf(), mutableListOf())

    // list used to hold lists of unique Categories by type being used
    private var uniqueCategoryLists : MutableList<List<String>>
            = mutableListOf(emptyList(), emptyList())

    // used to tell which page of ViewPager2 to scroll to
    private var typeChanged : Int = 0

    // instance of ViewModel
    private val categoryViewModel : CategoryViewModel by lazy {
        ViewModelProvider(this).get(CategoryViewModel::class.java)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?,
                              savedInstanceState : Bundle?) : View? {

        val view : View = inflater.inflate(R.layout.fragment_category, container, false)

        // initialize views
        circleIndicator     = view.findViewById(R.id.category_circle_indicator) as CircleIndicator3
        categoriesViewPager = view.findViewById(R.id.category_view_pager      ) as ViewPager2

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        categoryViewModel.expenseCategoriesLiveData.observe(viewLifecycleOwner, Observer {

            // clears list before adding names again since names can be added or dropped
            categoryNameLists[0].clear()
            it.forEach { category : Category ->

                categoryNameLists[0].add(category.category)
            }
            categoryLists[0] = it
            updateUI(categoryLists)
        })

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        categoryViewModel.incomeCategoriesLiveData.observe(viewLifecycleOwner, Observer {

            // clears list before adding names again since names can be added or dropped
            categoryNameLists[1].clear()
            it.forEach { category : Category ->

                categoryNameLists[1].add(category.category)
            }
            categoryLists[1] = it
            updateUI(categoryLists)
        })

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        categoryViewModel.uniqueExpenseLiveData.observe(viewLifecycleOwner, Observer {

            // list of Categories used by Transactions
            uniqueCategoryLists[0] = it
            updateUI(categoryLists)
        })

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        categoryViewModel.uniqueIncomeLiveData.observe(viewLifecycleOwner, Observer {

            // list of Categories used by Transactions
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

        // creates Adapter with MutableList of Lists of Categories and sets it to ViewPager2
        categoriesViewPager.adapter = CategoryListAdapter(categoryLists)
        categoriesViewPager.setCurrentItem(typeChanged, false)
        // sets ViewPager2 to CircleIndicator
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

            val view : View = layoutInflater.inflate(R.layout.item_view_category_list,
                parent, false)
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
     *  ViewHolder stores a reference to an item's view.
     *
     *  @param view ViewPager2 layout.
     */
    private inner class CategoryListHolder(view : View) : RecyclerView.ViewHolder(view) {

        // views in ItemView
        private val categoryTypeTextView : TextView     = itemView.findViewById(R.id.category_type         )
        private val categoryRecyclerView : RecyclerView = itemView.findViewById(R.id.category_recycler_view)

        fun bind(categoryList : List<Category>, type : Int) {

            if (type == 0) {

                categoryTypeTextView.text = getString(R.string.type_expense)
            } else {

                categoryTypeTextView.text = getString(R.string.type_income)
            }
            val linearLayoutManager = LinearLayoutManager(context)
            // RecyclerView NEEDS a LayoutManager to work
            categoryRecyclerView.layoutManager = linearLayoutManager
            // set adapter for RecyclerView
            categoryRecyclerView.adapter = CategoryAdapter(categoryList)
            // adds horizontal divider between each item in RecyclerView
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

                val view : View = layoutInflater.inflate(R.layout.item_view_category,
                    parent, false)
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
         *  ViewHolder stores a reference to an item's.
         *
         *  @param view ItemView layout.
         */
        private inner class CategoryHolder(view : View) : RecyclerView.ViewHolder(view) {

            // views in ItemView
            private val editButton       : MaterialButton = itemView.findViewById(R.id.category_edit  )
            private val deleteButton     : MaterialButton = itemView.findViewById(R.id.category_delete)
            private val categoryTextView : TextView       = itemView.findViewById(R.id.category_name  )

            @SuppressLint("StringFormatInvalid")
            fun bind(category : Category) {

                val type : Int = when (category.type) {

                    "Expense" -> 0
                    else      -> 1
                }

                categoryTextView.text = category.category

                // enables delete button if Category is not in use and if there is more than 1 Category
                if (!uniqueCategoryLists[type].contains(category.category)
                    && categoryNameLists[type].size > 1) {

                    deleteButton.isEnabled = true

                    // AlertDialog to ensure user does want to delete Category
                    deleteButton.setOnClickListener {

                        val posFun = DialogInterface.OnClickListener { _, _ ->

                            deleteCategory(category)
                            typeChanged = type
                        }

                        AlertDialogCreator.alertDialog(context!!,
                            getString(R.string.alert_dialog_delete_category),
                            getString(R.string.alert_dialog_delete_warning, category.category),
                            getString(R.string.alert_dialog_yes), posFun,
                            getString(R.string.alert_dialog_no), AlertDialogCreator.doNothing)
                    }
                }

                // AlertDialog with EditText that allows input for new name
                editButton.setOnClickListener {

                    // inflates view that holds EditText
                    val viewInflated : View = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_input_field, view as ViewGroup, false)
                    // the EditText to be used
                    val input : EditText = viewInflated.findViewById(R.id.dialog_input)
                    val posFun = DialogInterface.OnClickListener { _, _ ->

                        editCategory(input.text.toString(), category, type)
                        typeChanged = type
                    }

                    AlertDialogCreator.alertDialogInput(context!!,
                        getString(R.string.alert_dialog_edit_category),
                        viewInflated,
                        getString(R.string.alert_dialog_save), posFun,
                        getString(R.string.alert_dialog_cancel), AlertDialogCreator.doNothing)
                }
            }

            /**
             *  @param category the Category to be deleted
             */
            private fun deleteCategory(category : Category) {

                launch {

                    categoryViewModel.deleteCategory(category)
                }
            }

            /**
             *  Checks if name inputted exists already before editing.
             *
             *  @param updatedName new name of Category
             *  @param category    Category to be changed
             *  @param type        used to tell which name list to check (Expense/Income)
             */
            private fun editCategory(updatedName : String, category : Category, type : Int) {

                // if exists, Snackbar appears telling user so, else, updates Category
                if (categoryNameLists[type].contains(updatedName)) {

                    val existBar : Snackbar = Snackbar.make(view!!,
                        getString(R.string.snackbar_exists, updatedName), Snackbar.LENGTH_SHORT)
                    existBar.anchorView = circleIndicator
                    existBar.show()
                } else {

                    category.category = updatedName
                    launch {

                        categoryViewModel.updateCategory(category)
                    }
                }
            }
        }
    }

    companion object {

        /**
         *  Initializes instance of CategoryFragment
         */
        fun newInstance() : CategoryFragment {

            return CategoryFragment()
        }
    }
}