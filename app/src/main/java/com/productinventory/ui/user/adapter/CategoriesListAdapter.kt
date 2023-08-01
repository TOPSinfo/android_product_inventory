package com.productinventory.ui.user.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.databinding.IteamCategoriesListBinding
import com.productinventory.ui.user.model.category.CategoriesModel
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible

class CategoriesListAdapter(
    private var categoriesList: ArrayList<CategoriesModel>,
    private val onClickItem: OnclickItem
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolder(var viewBinding: IteamCategoriesListBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            IteamCategoriesListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    interface OnclickItem {
        fun onRowClick(position: Int)
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ItemViewHolder

        if (!TextUtils.isEmpty(categoriesList[position].name)) {
            holder.viewBinding.btnName.text = categoriesList[position].name
            holder.viewBinding.btnName.makeVisible()
        } else {
            holder.viewBinding.btnName.makeGone()
        }

        holder.viewBinding.btnName.setOnClickListener {
            onClickItem.onRowClick(position)
        }
    }
}

