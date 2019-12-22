package com.heyzeusv.plutuswallet.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
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
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.viewmodels.CategoriesViewModel
import me.relex.circleindicator.CircleIndicator3

private const val TAG = "PWCategoriesFragment"

class CategoriesFragment : BaseFragment() {

    // views
    private lateinit var circleIndicator     : CircleIndicator3
    private lateinit var categoriesViewPager : ViewPager2

    // lists used to hold CategoryTotals and Category names
    private var categoryLists   : MutableList<List<String>> = mutableListOf(emptyList(), emptyList())
    private var expenseNameList : MutableList<String>       = mutableListOf()
    private var incomeNameList  : MutableList<String>       = mutableListOf()

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

        categoriesViewModel.expenseNamesLiveData.observe(this, Observer {

            categoryLists[0] = it
            updateUI(categoryLists)
        })

        categoriesViewModel.incomeNamesLiveData.observe(this, Observer {

            categoryLists[1] = it
            updateUI(categoryLists)
        })
    }

    /**
     *  Ensures the UI is up to date with correct information.
     *
     *  @param categoryLists list of lists to be shown in RecyclerViews.
     */
    private fun updateUI(categoryLists : MutableList<List<String>>) {

        categoriesViewPager.adapter = CategoryListAdapter(categoryLists)
        circleIndicator.setViewPager(categoriesViewPager)
    }

    /**
     *  Creates ViewHolder and binds ViewHolder to data from model layer.
     *
     *  @param categoryLists the list of lists of Categories.
     */
    private inner class CategoryListAdapter(var categoryLists : MutableList<List<String>>)
        : RecyclerView.Adapter<CategoryListHolder>() {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListHolder {

            val view : View = layoutInflater.inflate(R.layout.item_view_category_list, parent, false)
            return CategoryListHolder(view)
        }

        override fun getItemCount() : Int = categoryLists.size

        // populates given holder with list of Category names from the given position in list
        override fun onBindViewHolder(holder: CategoryListHolder, position: Int) {

            val categoryList : List<String> = categoryLists[position]
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

        fun bind(categoryList : List<String>, type : Int) {

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
        private inner class CategoryAdapter(var categoryList : List<String>)
            : RecyclerView.Adapter<CategoryHolder>() {

            // creates view to display, wraps the view in a ViewHolder and returns the result
            override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : CategoryHolder {

                val view : View = layoutInflater.inflate(R.layout.item_view_category, parent, false)
                return CategoryHolder(view)
            }

            override fun getItemCount() : Int = categoryList.size

            // populates given holder with Category from the given position in CategoryList
            override fun onBindViewHolder(holder : CategoryHolder, position : Int) {

                val categoryName : String = categoryList[position]
                holder.bind(categoryName)
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

            fun bind(categoryName : String) {

                categoryTextView.text = categoryName

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

                            editCategory(input.text.toString())
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

            private fun editCategory(updatedName : String) {


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