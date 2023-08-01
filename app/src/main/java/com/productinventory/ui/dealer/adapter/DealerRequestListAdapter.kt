package com.productinventory.ui.dealer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.R
import com.productinventory.databinding.RawRequestListBinding
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.dateToStringFormat

class DealerRequestListAdapter(
    private val mContext: Context,
    private var mList: ArrayList<BookingModel>,
    private val viewHolderClicks: ViewHolderClicks
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mTimeFormat: String = "hh:mm a"
    var mDateFormat: String = "MMM d, yyyy"

    class ItemViewHolder(var viewBinding: RawRequestListBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            RawRequestListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: BookingModel, position: Int, status: String)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ItemViewHolder
        val model = mList[position]

        try {
            holder.viewBinding.tvBookingWith.text = mContext.getString(
                R.string.appointment_with,
                model.userName.substringBefore(" ")
            )
            holder.viewBinding.txtProductName.text = model.productname
            holder.viewBinding.txtDate.text = model.startTime!!.dateToStringFormat(mDateFormat)
            holder.viewBinding.txtTime.text = mContext.getString(
                R.string.time_user_time,
                model.startTime!!.dateToStringFormat(mTimeFormat),
                model.endTime!!.dateToStringFormat(mTimeFormat)
            )

            holder.viewBinding.imgAccept.setOnClickListener {
                viewHolderClicks.onClickItem(model, position, Constants.APPROVE_STATUS)
            }

            holder.viewBinding.imgReject.setOnClickListener {
                viewHolderClicks.onClickItem(model, position, Constants.REJECT_STATUS)
            }
        } catch (e: Exception) {
            MyLog.e("Exception", e.toString())
        }
    }
}

