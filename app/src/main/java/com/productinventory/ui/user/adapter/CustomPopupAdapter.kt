package com.productinventory.ui.user.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.R
import com.productinventory.utils.Constants

/**
 * Custom Popup list adapter : Show item list in popup
 */
class CustomPopupAdapter(
    private val mContext: Context,
    private val customItems: ArrayList<String>,
    private val delegate: Delegate
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Delegate {
        fun onCustomItemClick(customItem: String, position: Int)
    }

    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            Constants.RADIO_TYPE -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.raw_time_popup_list, parent, false)
                return RadioViewHolder(v)
            }
            Constants.TEXTVIEW_TYPE -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.raw_time_popup_list, parent, false)
                return TextViewHolder(v)
            }
            Constants.CHECKBOX_TYPE -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.raw_time_popup_list, parent, false)
                return CheckBoxViewHolder(v)
            }
            Constants.CUSTOM_TYPE -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.raw_time_popup_list, parent, false)
                return CustomViewHolder(v)
            }
        }
        val v =
            mInflater.inflate(R.layout.raw_time_popup_list, parent, false)
        return CustomViewHolder(
            v
        )
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = this.customItems[position]
        // for default check in first item
        when (holder) {
            is RadioViewHolder -> {
                // empty
            }
            is TextViewHolder -> {
                // empty
            }
            is CheckBoxViewHolder -> {
                // empty
            }
            is CustomViewHolder -> {
                holder.tvTitle.text = model
                holder.itemView.setOnClickListener {
                    delegate.onCustomItemClick(model, position)
                }
            }
        }
    }

    override fun getItemCount() = this.customItems.size

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val view: View = itemView.findViewById(R.id.view)
        val view2: View = itemView.findViewById(R.id.view2)
    }

    class RadioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class CheckBoxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
