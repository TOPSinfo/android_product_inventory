package com.productinventory.ui.dealer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.R
import com.productinventory.databinding.RawBookingDealerListBinding
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.dateToStringFormat

class DealerBookingListAdapter(
    private val mContext: Context,
    var mList: ArrayList<BookingModel>,
    private val viewHolderClicks: ViewHolderClicks
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mTimeFormat: String = "hh:mm a"
    var mDateFormat: String = "d\nMMM"

    class ItemViewHolder(var viewBinding: RawBookingDealerListBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RawBookingDealerListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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
            holder.viewBinding.tvDate.text = model.startTime!!.dateToStringFormat(mDateFormat)
            holder.viewBinding.tvTime.text = mContext.getString(
                R.string.time_user_time,
                model.startTime!!.dateToStringFormat(mTimeFormat),
                model.endTime!!.dateToStringFormat(mTimeFormat)
            )

            holder.viewBinding.clMain.setOnClickListener {
                viewHolderClicks.onClickItem(model, position, Constants.RAW_CLICK)
            }

            var mColor = mContext.getColor(R.color.pending_status)
            var mStatus = mContext.getString(R.string.waiting)
            when (model.status) {
                Constants.COMPLETED_STATUS -> {
                    mStatus = mContext.getString(R.string.completed)
                    mColor = mContext.getColor(R.color.completed_status)
                }
                Constants.APPROVE_STATUS -> {
                    mStatus = mContext.getString(R.string.approved)
                    mColor = mContext.getColor(R.color.approve_status)
                }
                Constants.REJECT_STATUS -> {
                    mStatus = mContext.getString(R.string.rejected)
                    mColor = mContext.getColor(R.color.reject_status)
                }
                Constants.PENDING_STATUS -> {
                    mStatus = mContext.getString(R.string.waiting)
                    mColor = mContext.getColor(R.color.pending_status)
                }
                Constants.CANCEL_STATUS -> {
                    mStatus = mContext.getString(R.string.deleted)
                    mColor = mContext.getColor(R.color.delete_status)
                }
            }
            holder.viewBinding.tvStatus.text = mStatus
            holder.viewBinding.imgColor.setBackgroundColor(mColor)
            holder.viewBinding.tvStatus.setTextColor(mColor)
        } catch (e: Exception) {
            MyLog.e("Exception", e.toString())
        }
    }
}

