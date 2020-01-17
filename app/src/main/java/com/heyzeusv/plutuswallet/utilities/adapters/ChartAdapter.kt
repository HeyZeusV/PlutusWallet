package com.heyzeusv.plutuswallet.utilities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heyzeusv.plutuswallet.database.entities.ItemViewChart
import com.heyzeusv.plutuswallet.databinding.ItemViewChartBinding

/**
 *  Creates ViewHolder and binds ViewHolder with data from ViewModel for ChartFragment.
 *
 *  @param ivcList list of ItemViewChart holding data to create charts.
 */
class ChartAdapter(var ivcList : List<ItemViewChart>)
    : RecyclerView.Adapter<ChartHolder>() {

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ChartHolder {

        val itemViewBinding : ItemViewChartBinding = ItemViewChartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ChartHolder(itemViewBinding)
    }

    override fun getItemCount() : Int = ivcList.size

    override fun onBindViewHolder(holder : ChartHolder, position : Int) {

        val ivc : ItemViewChart = ivcList[position]
        holder.bind(ivc)
    }
}

/**
 *  @param binding DataBinding layout
 */
class ChartHolder(var binding : ItemViewChartBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(ivc : ItemViewChart) {

        binding.ivc = ivc
        binding.executePendingBindings()
    }
}