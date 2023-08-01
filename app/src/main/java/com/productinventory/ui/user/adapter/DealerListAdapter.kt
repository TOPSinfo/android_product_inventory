package com.productinventory.ui.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.databinding.IteamDealerListBinding
import com.productinventory.ui.dealer.authentication.model.user.DealerModel
import com.productinventory.utils.loadImageWithoutCenterCrop

class DealerListAdapter(
    var dealerList: ArrayList<DealerModel>,
    val onclickItem: OnclickItem
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolder(var viewBinding: IteamDealerListBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            IteamDealerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    interface OnclickItem {
        fun onRowClick(position: Int)
    }

    override fun getItemCount(): Int {
        return dealerList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val holder = holder as ItemViewHolder

        holder.viewBinding.view.visibility = if (position % 2 == 1) View.GONE else View.VISIBLE
        holder.viewBinding.rrMainRoot.setOnClickListener {
            onclickItem.onRowClick(position)
        }
        holder.viewBinding.imgThumb.loadImageWithoutCenterCrop(dealerList[position].image)
    }
}

