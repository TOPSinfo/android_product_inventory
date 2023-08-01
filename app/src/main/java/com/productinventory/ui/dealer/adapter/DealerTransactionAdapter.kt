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
import com.productinventory.ui.user.model.wallet.WalletModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.dateToStringFormat

/**
 * Transaction list adapter : Show Transaction list.
 */
class DealerTransactionAdapter(
    private val mContext: Context,
    private var mList: ArrayList<WalletModel>,
    private val viewHolderClicks: ViewHolderClicks,
) : RecyclerView.Adapter<DealerTransactionAdapter.ViewHolder>() {
    var mDateFormat: String = "d MMM, hh:mm a"
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v =
            mInflater.inflate(R.layout.raw_dealer_transaction, parent, false)
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
            val mTranscation: String
            val mImage: Int
            if (model.isRefund) {

                mImage = R.drawable.ic_paid_money
                mTranscation = mContext.getString(
                    R.string.send_refund_user,
                    model.userName.substringBefore(" ")
                )
                holder.tvOperand.text = "-"
                holder.tvPaymentBy.text = mContext.getString(R.string.sent_from)
                if (model.paymentType == Constants.PAYMENT_TYPE_RAZOR_PAY) {
                    holder.imgPaymentBy.setImageResource(R.drawable.ic__credit_card)
                }
            } else {
                mImage = R.drawable.ic_add_money
                mTranscation = mContext.getString(
                    R.string.received_from_user,
                    model.userName.substringBefore(" ")
                )
                holder.tvOperand.text = "+"
                holder.tvPaymentBy.text = mContext.getString(R.string.receive_in)
            }
            holder.imgRupee.setImageResource(mImage)
            holder.tvTransaction.text = mTranscation
            holder.tvTime.text = model.createdAt?.toDate()?.dateToStringFormat(mDateFormat)
            holder.tvOrderID.visibility = if (model.orderid.isNotEmpty()) View.VISIBLE else View.GONE
            holder.tvOrderID.text = "${mContext.getString(R.string.order_id)}${model.orderid}"
            holder.tvAmount.text = model.amount.toString().removePrefix("-")
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
        var imgRupee: ImageView = itemView.findViewById(R.id.imgRupee)
        var tvTransaction: TextView = itemView.findViewById(R.id.tvTransaction)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        var tvOrderID: TextView = itemView.findViewById(R.id.tvOrderID)
        var tvOperand: TextView = itemView.findViewById(R.id.tvOperand)
        var tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        var imgPaymentBy: ImageView = itemView.findViewById(R.id.imgPaymentBy)
        var tvPaymentBy: TextView = itemView.findViewById(R.id.tvPaymentBy)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: WalletModel, position: Int)
    }
}
