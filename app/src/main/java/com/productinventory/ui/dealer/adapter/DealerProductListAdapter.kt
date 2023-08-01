package com.productinventory.ui.dealer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.R
import com.productinventory.databinding.RawProductListBinding
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.utils.Constants
import com.productinventory.utils.loadImageWithoutCenterCrop

class DealerProductListAdapter(
    private val ctx: Context,
    private var list: ArrayList<ProductModel>,
    private val onClickItem: OnclickItem
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolder(var viewBinding: RawProductListBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            RawProductListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    interface OnclickItem {
        fun onRowClick(position: Int, productModel: ProductModel, listner: String)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ItemViewHolder

        holder.viewBinding.imgProduct.loadImageWithoutCenterCrop(list[position].image)

        holder.viewBinding.txtShortDesc.text = list[position].name.trim()

        holder.viewBinding.tvProductPrice.text =
            ctx.getString(R.string.rupee) + " " + list[position].price.trim()

        holder.viewBinding.imgEdit.setOnClickListener {
            onClickItem.onRowClick(position, list[position], Constants.EDIT_DATA)
        }

        holder.viewBinding.imgDelete.setOnClickListener {
            onClickItem.onRowClick(position, list[position], Constants.DELETE_DATA)
        }
    }
}

