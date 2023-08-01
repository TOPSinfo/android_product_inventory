package com.productinventory.ui.dealer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.databinding.ItemPhotoSelectBinding
import com.productinventory.utils.loadImageWithoutCenterCrop

class PhotoSelectListAdapter(
    private var list: ArrayList<String>,
    private val onClickItem: OnclickItem
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolder(var viewBinding: ItemPhotoSelectBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            ItemPhotoSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    interface OnclickItem {
        fun onRowClick(position: Int)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ItemViewHolder

        holder.viewBinding.imgSelected.loadImageWithoutCenterCrop(list[position])

        holder.viewBinding.imgRemove.setOnClickListener {
            onClickItem.onRowClick(position)
        }
    }
}

