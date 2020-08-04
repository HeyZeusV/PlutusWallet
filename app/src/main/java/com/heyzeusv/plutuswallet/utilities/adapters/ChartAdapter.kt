package com.heyzeusv.plutuswallet.utilities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heyzeusv.plutuswallet.database.entities.ItemViewChart
import com.heyzeusv.plutuswallet.databinding.ItemViewChartBinding
import com.heyzeusv.plutuswallet.utilities.ChartDiffUtil

/**
 *  Creates ViewHolder and binds ViewHolder with data from ViewModel for ChartFragment.
 *
 */
class ChartAdapter : ListAdapter<ItemViewChart, ChartHolder>(ChartDiffUtil()) {

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ChartHolder {

        val itemViewBinding : ItemViewChartBinding = ItemViewChartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ChartHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder : ChartHolder, position : Int) {

        val ivc : ItemViewChart = getItem(position)
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