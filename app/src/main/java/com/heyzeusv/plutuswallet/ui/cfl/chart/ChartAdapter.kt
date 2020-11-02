package com.heyzeusv.plutuswallet.ui.cfl.chart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heyzeusv.plutuswallet.data.model.ItemViewChart
import com.heyzeusv.plutuswallet.databinding.ItemViewChartBinding

/**
 *  Adapter for Charts.
 */
class ChartAdapter : ListAdapter<ItemViewChart, ChartAdapter.ChartHolder>(ChartDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartHolder {

        val itemViewBinding: ItemViewChartBinding = ItemViewChartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChartHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: ChartHolder, position: Int) {

        val ivc: ItemViewChart = getItem(position)
        holder.bind(ivc)
    }

    class ChartHolder(var binding: ItemViewChartBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ivc: ItemViewChart) {

            binding.ivc = ivc
            binding.executePendingBindings()
        }
    }
}

/**
 *  Callback for calculating the diff between two non-null items in a list.
 */
class ChartDiffUtil : DiffUtil.ItemCallback<ItemViewChart>() {

    override fun areItemsTheSame(oldItem: ItemViewChart, newItem: ItemViewChart): Boolean {

        return oldItem.fType == newItem.fType
    }

    override fun areContentsTheSame(oldItem: ItemViewChart, newItem: ItemViewChart): Boolean {

        return oldItem == newItem
    }
}