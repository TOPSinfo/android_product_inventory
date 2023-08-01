package com.productinventory.ui.user.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.R
import com.productinventory.databinding.IteamProductListBinding
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.utils.loadImageWithoutCenterCrop

class ProductListAdapter(val ctx: Context,
                         var productList: ArrayList<ProductModel>,
                         val onclickItem: OnclickItem) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var animation: Animation = AnimationUtils.loadAnimation(ctx, R.anim.bounce_anim)

    class ItemViewHolder(var viewBinding: IteamProductListBinding) : RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = IteamProductListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ItemViewHolder

        holder.viewBinding.rrMainRoot.setOnClickListener {
            onclickItem.onRowClick(position, isWish = false, isRow = true)
        }
        if (productList[position].isWishList) {
            holder.viewBinding.imgLike.setImageResource(R.drawable.ic_like_small)
        } else {
            holder.viewBinding.imgLike.setImageResource(R.drawable.ic_unlike_small)
        }
        holder.viewBinding.imgLike.setOnClickListener {
            if (productList[position].isWishList) {
                onclickItem.onRowClick(position, isWish = false, isRow = false)
            } else {
                onclickItem.onRowClick(position, isWish = true, isRow = false)
            }
            holder.viewBinding.imgLike.startAnimation(animation)
        }

        holder.viewBinding.imgThumb.loadImageWithoutCenterCrop(productList[position].image)
        holder.viewBinding.txtProductName.text = productList[position].name.trim()
        holder.viewBinding.txtProductPrice.text = ctx.getString(R.string.rupee) + " " + productList[position].price.trim()
    }

    interface OnclickItem {
        fun onRowClick(position: Int, isWish: Boolean, isRow: Boolean)
    }
}

