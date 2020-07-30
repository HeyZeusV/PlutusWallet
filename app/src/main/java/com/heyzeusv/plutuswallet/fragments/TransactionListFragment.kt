package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.billingrepo.localdb.NoAds
import com.heyzeusv.plutuswallet.database.entities.ItemViewTransaction
import com.heyzeusv.plutuswallet.database.entities.TransactionInfo
import com.heyzeusv.plutuswallet.databinding.FragmentTransactionListBinding
import com.heyzeusv.plutuswallet.databinding.ItemViewTransactionBinding
import com.heyzeusv.plutuswallet.utilities.AlertDialogCreator
import com.heyzeusv.plutuswallet.viewmodels.BillingViewModel
import com.heyzeusv.plutuswallet.viewmodels.CFLViewModel
import com.heyzeusv.plutuswallet.viewmodels.TransactionListViewModel
import kotlinx.coroutines.launch

private const val TAG          = "PWTransactionListFrag"
private const val TEST_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
private const val AD_UNIT_ID   = "ca-app-pub-7627627324882759/8027617303"

/**
 *  Will show list of Transactions depending on filters applied.
 */
class TransactionListFragment : BaseFragment() {

    /**
     *  Required interface for hosting fragments.
     *
     *  Defines work that the fragment needs done by hosting activity.
     */
    interface Callbacks {

        /**
         *  Replaces TransactionListFragment, FilterFragment, and ChartFragment
         *  with TransactionFragment selected.
         *
         *  @param transactionId id of Transaction selected.
         *  @param fromFab       true if user clicked on FAB to create Transaction.
         */
        fun onTransactionSelected(transactionId : Int, fromFab : Boolean)
    }

    private var callbacks : Callbacks? = null

    // DataBinding
    private lateinit var binding : FragmentTransactionListBinding

    // views
    private lateinit var adContainer : RelativeLayout

    // provides instance of ViewModel
    private val listVM : TransactionListViewModel by lazy {
        ViewModelProviders.of(this).get(TransactionListViewModel::class.java)
    }

    // shared ViewModels
    private lateinit var cflViewModel     : CFLViewModel
    private lateinit var billingViewModel : BillingViewModel

    override fun onAttach(context : Context) {
        super.onAttach(context)

        // stashing context into callbacks property which is the activity instance hosting fragment
        callbacks = context as Callbacks?
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?,
                              savedInstanceState : Bundle?) : View? {

        //setting up DataBinding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_transaction_list, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.listVM         = listVM

        val view : View = binding.root

        // LayoutManager requires context so created here and sent to ViewModel
        listVM.layoutManager.value = LinearLayoutManager(context)

        // initialize views
        adContainer  = view.findViewById(R.id.adContainer              ) as RelativeLayout

        // this ensures that this is same CFLViewModel as Filter/ChartFragment use
        cflViewModel = activity!!.let {

            ViewModelProviders.of(it).get(CFLViewModel::class.java)
        }
        billingViewModel = activity!!.let {

            ViewModelProviders.of(it).get(BillingViewModel::class.java)
        }

        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        cflViewModel.tInfoLiveData.observe(viewLifecycleOwner, Observer { tInfo : TransactionInfo ->

            listVM.ivtList = listVM.filteredTransactionList(tInfo.account, tInfo.category, tInfo.date,
                tInfo.type, tInfo.accountName, tInfo.categoryName, tInfo.start, tInfo.end)

            // register an observer on LiveData instance and tie life to this component
            // execute code whenever LiveData gets update
            listVM.ivtList.observe(viewLifecycleOwner, Observer { transactions : List<ItemViewTransaction> ->

                listVM.ivtEmpty.value = transactions.isEmpty()
                if (cflViewModel.filterChanged) {

                    listVM.recyclerViewPosition.value = transactions.size - 1
                    cflViewModel.filterChanged = false
                }
                listVM.tranAdapter.value = TranListAdapter(transactions)
            })
        })

        listVM.initializeTables()

        // When FAB is pressed, a new blank TransactionFragment will be created and displayed
        listVM.fabOnClick.value = View.OnClickListener {

            val transFrag = TransactionFragment(0, true)

            activity!!.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                    R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.fragment_transaction_container, transFrag)
                .addToBackStack(null)
                .commit()
        }

        // register an observer on LiveData instance and tie life to this component
        // execute code whenever LiveData gets updated
        billingViewModel.noAdsLiveData.observe(this, Observer { noAds : NoAds? ->

            // will load ads if there is noAds data or if user is not entitled to NoAds
            if (noAds == null) {

                loadAd(view)
            } else {

                // makes ad disappear
                if (noAds.entitled) {

                    adContainer.removeAllViews()

                } else {

                    loadAd(view)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // ensures that RecyclerView is up to date with any symbols changes
        listVM.tranAdapter.value?.notifyDataSetChanged()
        listVM.futureTransactions()
    }

    override fun onDetach() {
        super.onDetach()

        // afterward you cannot access the activity
        // or count on the activity continuing to exist
        callbacks = null
    }

    /**
     *  Creates AdView with an Adaptive Banner size
     *
     *  Determines the width of the device using DisplayMetrics and passes it to a AdSize, which
     *  automatically decides the height of the ad depending on the device. Lastly, the AdView is
     *  created and added to RelativeLayout container.
     *
     *  @param view the layout of this fragment.
     */
    private fun loadAd(view : View) {

        // information about device screen
        val metrics : DisplayMetrics = Resources.getSystem().displayMetrics
        // device screen density
        val density : Float = metrics.density

        // width of ad in pixels(width of screen)
        var adWidthPixels : Float = view.width.toFloat()
        if (adWidthPixels == 0f) {

            adWidthPixels = metrics.widthPixels.toFloat()
        }

        // width in pixels / screen density
        val adWidth : Int = (adWidthPixels / density).toInt()
        // only requires width, will automatically set height according to device
        val adSize : AdSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)

        // just in case, remove an ads that might exist
        adContainer.removeAllViews()

        // create new AdView with adSize and adUnitID
        val adView      = AdView(context)
        adView.adSize   = adSize
        adView.adUnitId = TEST_UNIT_ID

        // add AdView to container
        adContainer.addView(adView)

        // loads ad
        val adRequest : AdRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    /**
     *  Creates ViewHolder and binds ViewHolder to data from model layer.
     *
     *  @param ivtList the list of ItemViewTransaction holding data to display Transactions.
     */
     inner class TranListAdapter(private var ivtList : List<ItemViewTransaction>)
        : RecyclerView.Adapter<TranListHolder>() {

        // creates view to display, wraps the view in a ViewHolder and returns the result
        override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : TranListHolder {

            val itemViewBinding : ItemViewTransactionBinding = ItemViewTransactionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return TranListHolder(itemViewBinding)
        }

        override fun getItemCount() : Int = ivtList.size

        // populates given holder with Transaction from the given position in list
        override fun onBindViewHolder(holder : TranListHolder, position : Int) {

            val ivt : ItemViewTransaction = ivtList[position]
            holder.bind(ivt)
        }
    }

    /**
     *  ViewHolder stores a reference to an item's view.
     *
     *  @param binding DataBinding layout
     */
     inner class TranListHolder(var binding : ItemViewTransactionBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {

            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        // sets the views with Transaction data
        fun bind(ivt : ItemViewTransaction) {

            binding.ivt = ivt
            binding.executePendingBindings()

            // formats the Total correctly
            when {

                decimalPlaces && symbolSide -> binding.transactionTotal.text = getString(
                    R.string.total_number_symbol, currencySymbol, decimalFormatter.format(ivt.total))
                decimalPlaces -> binding.transactionTotal.text = getString(
                    R.string.total_number_symbol, decimalFormatter.format(ivt.total), currencySymbol)
                symbolSide -> binding.transactionTotal.text = getString(
                    R.string.total_number_symbol, currencySymbol, integerFormatter.format(ivt.total))
                else -> binding.transactionTotal.text = getString(
                    R.string.total_number_symbol, integerFormatter.format(ivt.total), currencySymbol)
            }
        }

        override fun onClick(v : View?) {

            // the position that the user clicked on
            // doesn't scroll to correct position for first few items when using findLast...()
            when {

                layoutPosition < 5 -> listVM.recyclerViewPosition.value = layoutPosition
                else -> listVM.recyclerViewPosition.value =
                    listVM.layoutManager.value?.findLastCompletelyVisibleItemPosition()
            }
            // notifies hosting activity which item was selected
            callbacks?.onTransactionSelected(binding.ivt!!.id, false)
        }

        /**
         *  Shows AlertDialog asking user if they want to delete Transaction.
         */
        @SuppressLint("StringFormatInvalid")
        override fun onLongClick(v : View?) : Boolean {

            val posFun = DialogInterface.OnClickListener { _, _ ->

                launch {

                    // the position that the user clicked on
                    // doesn't scroll to correct position for first few items when using findLast...()
                    when {

                        layoutPosition < 5 -> listVM.recyclerViewPosition.value = layoutPosition
                        else -> listVM.recyclerViewPosition.value =
                            listVM.layoutManager.value?.findLastCompletelyVisibleItemPosition()
                    }
                    listVM.deleteTransaction(listVM.getTransactionAsync(binding.ivt!!.id).await())
                }
            }

            AlertDialogCreator.alertDialog(context!!,
                getString(R.string.alert_dialog_delete_transaction),
                getString(R.string.alert_dialog_delete_warning, binding.ivt!!.title),
                getString(R.string.alert_dialog_yes), posFun,
                getString(R.string.alert_dialog_no), AlertDialogCreator.doNothing)

            return true
        }
    }

    companion object {

        /**
         *  Initializes instance of TransactionListFragment.
         */
        fun newInstance() : TransactionListFragment {

            return TransactionListFragment()
        }
    }
}