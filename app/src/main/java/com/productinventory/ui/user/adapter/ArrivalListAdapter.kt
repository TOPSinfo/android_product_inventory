package com.productinventory.ui.user.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.R
import com.productinventory.databinding.IteamArrivalListBinding
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.utils.Constants
import com.productinventory.utils.loadImageWithoutCenterCrop

class ArrivalListAdapter(private val context: Context,
                         private var newArrivalList: ArrayList<ProductModel>,
                         private val onclickItem: OnclickItem) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolder(var viewBinding: IteamArrivalListBinding) : RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = IteamArrivalListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    interface OnclickItem {
        fun onRowClick(position: Int, isWish: Boolean)
    }

    override fun getItemCount(): Int {
        return if (newArrivalList.size > Constants.INT_10) Constants.INT_10 else newArrivalList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val holder = holder as ItemViewHolder

        holder.viewBinding.imgProduct.loadImageWithoutCenterCrop(newArrivalList[position].imageList[0])
        holder.viewBinding.txtCategoriesDec.text = newArrivalList[position].fullDescription.trim()
        holder.viewBinding.txtProductName.text = newArrivalList[position].name.trim()
        holder.viewBinding.txtProductPrice.text = context.getString(R.string.rupee) + " " + newArrivalList[position].price.trim()
        holder.viewBinding.cvMainRoot.setOnClickListener {
            onclickItem.onRowClick(position, false)
        }
    }
}

