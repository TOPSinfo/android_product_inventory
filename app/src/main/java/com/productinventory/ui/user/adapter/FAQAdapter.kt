package com.productinventory.ui.user.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.productinventory.R
import com.productinventory.ui.user.authentication.model.cms.FAQModel
import com.productinventory.utils.MyLog
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible

/**
 * FAQ list adapter : Show FAQ list.
 */
class FAQAdapter(
    private val mContext: Context,
    var mList: ArrayList<FAQModel>,
) : RecyclerView.Adapter<FAQAdapter.ViewHolder>() {
    var mCheckedPosition = -1
    var mInflater: LayoutInflater =
        mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = mInflater.inflate(R.layout.raw_faq, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = mList[position]
        try {

            if (position == mCheckedPosition) {
                holder.tvAnswer.makeVisible()
                holder.tvQuestion.setTextColor(mContext.getColor(R.color.user_theme))
                holder.imgDown.setImageResource(R.drawable.ic_up_arrow)
                holder.imgDown.setColorFilter(
                    ContextCompat.getColor(mContext, R.color.user_theme),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                holder.tvAnswer.makeGone()
                holder.tvQuestion.setTextColor(mContext.getColor(R.color.user_theme))
                holder.imgDown.setImageResource(R.drawable.ic_down_arrow)
                holder.imgDown.setColorFilter(
                    ContextCompat.getColor(mContext, R.color.user_theme),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            holder.tvQuestion.text = model.title
            holder.tvAnswer.text = model.answer

            holder.itemView.setOnClickListener {
                mCheckedPosition = if (mCheckedPosition == position) -1 else position
                notifyDataSetChanged()
            }
        } catch (e: Exception) {
            MyLog.e("Exception", e.toString())
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
        var tvAnswer: TextView = itemView.findViewById(R.id.tvAnswer)
        var imgDown: ImageView = itemView.findViewById(R.id.imgDown)
    }

    interface ViewHolderClicks {
        fun onClickItem(model: FAQModel, position: Int)
    }
}
