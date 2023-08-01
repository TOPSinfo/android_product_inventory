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
 * Event list adapter : Show Event list.
 */
class CalendarEventAdapter(
    private val mContext: Context,
    private var mList: ArrayList<BookingModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<CalendarEventAdapter.ViewHolder>() {
    var mTimeFormat: String = "hh:mm a"
    var mDateFormat: String = "d\nMMM"
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_calendar_event, parent, false)
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
                R.string.your_appointment_with_user,
                model.dealername.substringBefore(" ")
            )
            holder.tvTime.text = "${model.startTime!!.dateToStringFormat(mTimeFormat)} " +
                    "${" to "} " +
                    "${model.endTime!!.dateToStringFormat(mTimeFormat)}"

            /*holder.tvTime.text = "${model.startTime!!.dateToStringFormat(mTimeFormat)} ${
                model.endTime!!.dateToStringFormat(mTimeFormat)
            }"*/

            var mColor = mContext.getColor(R.color.pending_status)
            var image = R.drawable.ic_waiting

            when (model.status) {
                Constants.COMPLETED_STATUS -> {
                    image = R.drawable.ic_read_dealer
                    mColor = mContext.getColor(R.color.completed_status)
                }
                Constants.APPROVE_STATUS -> {
                    image = R.drawable.ic_check_black
                    mColor = mContext.getColor(R.color.user_theme)
                }
                Constants.REJECT_STATUS -> {
                    image = R.drawable.ic_close
                    mColor = mContext.getColor(R.color.user_theme)
                }
                Constants.PENDING_STATUS -> {
                    image = R.drawable.ic_waiting
                    mColor = mContext.getColor(R.color.pending_status)
                }
                Constants.CANCEL_STATUS -> {
                    image = R.drawable.ic_delete
                    mColor = mContext.getColor(R.color.delete_status)
                }
            }
            holder.imgColor.setBackgroundColor(mColor)
            holder.imgStatus.setImageResource(image)

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
        var imgStatus: ImageView = itemView.findViewById(R.id.imgEventType)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: BookingModel, position: Int)
    }
}
