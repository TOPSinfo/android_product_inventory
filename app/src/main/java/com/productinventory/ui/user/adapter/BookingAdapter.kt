package com.productinventory.ui.user.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.R
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.dateToStringFormat


/**
 * Community list adapter : Show Community list.
 */
class BookingAdapter(
    private val mContext: Context,
    private var mList: ArrayList<BookingModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {
    var mTimeFormat: String = "hh:mm a"
    var mDateFormat: String = "d\nMMM"
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_booking, parent, false)
        return ViewHolder(
            v
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val model = mList[position]
        try {
            holder.tvBookingWith.text = mContext.getString(
                R.string.with_astro_user,
                model.dealername
            )
            holder.tvDate.text = model.startTime!!.dateToStringFormat(mDateFormat)
            holder.tvTime.text = mContext.getString(
                R.string.time_user_time,
                model.startTime!!.dateToStringFormat(mTimeFormat),
                model.endTime!!.dateToStringFormat(mTimeFormat)
            )

            holder.tvCharge.text = mContext.getString(
                R.string.rate_user_charge, model.dealerpermincharge.toString()
            )

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
            holder.tvStatus.text = mStatus
            holder.imgColor.setBackgroundColor(mColor)
            holder.tvStatus.setTextColor(mColor)

            holder.itemView.setOnClickListener {
                viewHolderClicks.onClickItem(model, position)
            }
        } catch (e: Exception) {
            MyLog.e("Exception", e.toString())
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgColor: ImageView = itemView.findViewById(R.id.imgColor)
        var tvBookingWith: TextView = itemView.findViewById(R.id.tvBookingWith)
        var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        var tvCharge: TextView = itemView.findViewById(R.id.tvCharge)
        var tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: BookingModel, position: Int)
    }
}
