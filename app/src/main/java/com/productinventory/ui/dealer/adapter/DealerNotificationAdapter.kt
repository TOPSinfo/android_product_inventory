package com.productinventory.ui.dealer.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.R
import com.productinventory.ui.dealer.model.notification.NotificationModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.dateToStringFormat

/**
 * Notification list adapter : Show Notification list.
 */
class DealerNotificationAdapter(
    private var mContext: Context,
    private var mList: ArrayList<NotificationModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<DealerNotificationAdapter.ViewHolder>() {
    var mDateFormat: String = "d MMM"
    var mInflater: LayoutInflater = mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = mInflater.inflate(R.layout.raw_dealer_notification, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = mList[position]
        try {
            if (model.type == Constants.NOTIFICATION_REQUEST_ADDED_ACCEPTED) {
                // booking accepted or created
                holder.imgNotification.setImageResource(R.drawable.ic_notification_calendar_dealer)
            } else if (model.type == Constants.NOTIFICATION_REQUEST_ADDED_REJECTED) {
                // booking accepted or created
                holder.imgNotification.setImageResource(R.drawable.bg_edittext_border)
            } else {
                // reminder
                holder.imgNotification.setImageResource(R.drawable.ic_notification_reminder)
            }
            holder.tvNoticationTitle.text = model.title
            holder.tvDesc.text = model.message
            holder.tvTime.text = model.createdAt?.toDate()?.dateToStringFormat(mDateFormat)
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
        var imgNotification: ImageView = itemView.findViewById(R.id.imgNotification)
        var tvNoticationTitle: TextView = itemView.findViewById(R.id.tvNoticationTitle)
        var tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: NotificationModel, position: Int)
    }
}
