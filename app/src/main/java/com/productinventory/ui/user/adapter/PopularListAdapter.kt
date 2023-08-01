package com.productinventory.ui.user.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.R
import com.productinventory.databinding.IteamPopularListBinding
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.utils.Constants
import com.productinventory.utils.loadImageWithoutCenterCrop

class PopularListAdapter(private val context: Context,
                         private var popularList: ArrayList<ProductModel>,
                         private val onclickItem: OnclickItem) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolder(var viewBinding: IteamPopularListBinding) : RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = IteamPopularListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (popularList.size > Constants.INT_10) Constants.INT_10 else popularList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ItemViewHolder

        holder.viewBinding.imgProduct.loadImageWithoutCenterCrop(popularList[position].imageList[0])
        holder.viewBinding.txtCategoriesDec.text = popularList[position].fullDescription.trim()
        holder.viewBinding.txtProductName.text = popularList[position].name.trim()
        holder.viewBinding.txtProductPrice.text = context.getString(R.string.rupee) + " " +popularList[position].price.trim()
        holder.viewBinding.cvMainRoot.setOnClickListener {
            onclickItem.onRowClick(position, false)
        }
    }

    interface OnclickItem {
        fun onRowClick(position: Int, isWish: Boolean)
    }
}

