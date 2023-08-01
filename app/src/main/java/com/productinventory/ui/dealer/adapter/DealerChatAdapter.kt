package com.productinventory.ui.dealer.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.productinventory.R
import com.productinventory.ui.dealer.dealerDashboard.DealerChatActivity
import com.productinventory.ui.user.authentication.model.chat.MessagesModel
import com.productinventory.utils.Constants
import com.productinventory.utils.Utility
import com.productinventory.utils.loadImage

/**
 * Chat list adapter : Show message list.
 */
class DealerChatAdapter(
    private val context: DealerChatActivity,
    private val messageList: ArrayList<MessagesModel>,
    private val isGroup: Boolean,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = layoutInflater.inflate(R.layout.raw_chat_me_dealer, parent, false)
            MyMessagesViewHolder(view)
        } else {
            val view = layoutInflater.inflate(R.layout.raw_chat_other_dealer, parent, false)
            OtherViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OtherViewHolder) {
            holder.bindItems(context, messageList[position], isGroup)
        } else if (holder is MyMessagesViewHolder) {
            holder.bindItems(context, messageList[position])
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderId.equals(FirebaseAuth.getInstance().currentUser!!.uid)
        ) {
            0
        } else 1
    }

    class OtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var txtMessageOther: TextView = itemView.findViewById(R.id.txtMessageOther)
        private var txtDateTime: TextView = itemView.findViewById(R.id.txtDateTime)
        private var txtDateTime1: TextView = itemView.findViewById(R.id.txtDateTime1)
        private var llMessage: LinearLayout = itemView.findViewById(R.id.llMessage)
        private var imgMessage: ImageView = itemView.findViewById(R.id.imgMessage)
        private var imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)
        private var cardViewImage: CardView = itemView.findViewById(R.id.cardViewImage)
        private var txtSenderName: TextView = itemView.findViewById(R.id.txtSenderName)
        private var cardViewImageForSenderName: CardView =
            itemView.findViewById(R.id.cardViewImageForSenderName)

        fun bindItems(context: DealerChatActivity, chatModel: MessagesModel, isGroup: Boolean) {
            txtMessageOther.text = chatModel.message

            if (chatModel.timeStamp != null) {
                txtDateTime.text = Utility.getDate(chatModel.timeStamp!!.toDate().time)
                txtDateTime1.text = Utility.getDate(chatModel.timeStamp!!.toDate().time)
            }

            if (chatModel.messageType == Constants.TYPE_MESSAGE) {

                llMessage.visibility = View.VISIBLE
                imgMessage.visibility = View.GONE
                imgPlay.visibility = View.GONE
                cardViewImage.visibility = View.GONE
                cardViewImageForSenderName.visibility = View.GONE

                txtDateTime.visibility = View.VISIBLE
                txtDateTime1.visibility = View.GONE

                if (isGroup) {
                    txtSenderName.visibility = View.VISIBLE
                    txtSenderName.text = chatModel.senderName
                } else {
                    txtSenderName.visibility = View.GONE
                }
            } else if (chatModel.messageType == Constants.TYPE_IMAGE) {

                llMessage.visibility = View.GONE
                imgMessage.visibility = View.VISIBLE
                imgPlay.visibility = View.GONE
                cardViewImage.visibility = View.VISIBLE

                txtDateTime.visibility = View.GONE
                txtDateTime1.visibility = View.VISIBLE

                imgMessage.loadImage(chatModel.url)
            } else if (chatModel.messageType == Constants.TYPE_VIDEO) {

                llMessage.visibility = View.GONE
                imgMessage.visibility = View.VISIBLE
                imgPlay.visibility = View.VISIBLE
                cardViewImage.visibility = View.VISIBLE
                txtDateTime.visibility = View.GONE
                txtDateTime1.visibility = View.VISIBLE
                imgMessage.loadImage(chatModel.url)
            }

            imgMessage.setOnClickListener {
                if (chatModel.messageType == Constants.TYPE_IMAGE) {
                    context.redirectImageViewActivity(chatModel.url.toString())
                } else {
                    context.redirectVideoPlayActivity(chatModel.videoUrl.toString())
                }
            }
        }
    }

    class MyMessagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var txtMessage: TextView = itemView.findViewById(R.id.txtMessage)
        private var txtDateTime: TextView = itemView.findViewById(R.id.txtDateTime)
        private var txtDateTime1: TextView = itemView.findViewById(R.id.txtDateTime1)
        private var llMessage: LinearLayout = itemView.findViewById(R.id.llMessage)
        private var lnMessageTime: LinearLayout = itemView.findViewById(R.id.lnMessageTime)
        private var imgMessage: ImageView = itemView.findViewById(R.id.imgMessage)
        private var imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)

        private var cardViewImage: CardView = itemView.findViewById(R.id.cardViewImage)
        private var imgMessageStatus: ImageView = itemView.findViewById(R.id.imgMessageStatus)
        private var imgMessageStatus1: ImageView = itemView.findViewById(R.id.imgMessageStatus1)
        private var cardViewImageForSenderName: CardView =
            itemView.findViewById(R.id.cardViewImageForSenderName)
        private var lnImageTime: LinearLayout = itemView.findViewById(R.id.lnImageTime)
        private var progressBarImage: CircularProgressIndicator =
            itemView.findViewById(R.id.pbImage)
        val time: Int = Constants.INT_20

        fun bindItems(
            context: DealerChatActivity,
            chatModel: MessagesModel,
        ) {
            txtMessage.text = chatModel.message

            if (chatModel.timeStamp != null) {
                txtDateTime.text = Utility.getDate(chatModel.timeStamp!!.toDate().time)
                txtDateTime1.text = Utility.getDate(chatModel.timeStamp!!.toDate().time)
            }

            when {
                chatModel.messageType == Constants.TYPE_MESSAGE || chatModel.messageType.isNullOrBlank() -> {

                    llMessage.visibility = View.VISIBLE
                    lnMessageTime.visibility = View.VISIBLE
                    imgMessage.visibility = View.GONE
                    imgPlay.visibility = View.GONE
                    cardViewImage.visibility = View.GONE
                    cardViewImageForSenderName.visibility = View.GONE
                    lnImageTime.visibility = View.GONE
                }
                chatModel.messageType == Constants.TYPE_IMAGE -> {

                    llMessage.visibility = View.GONE
                    lnMessageTime.visibility = View.GONE
                    imgMessage.visibility = View.VISIBLE
                    imgPlay.visibility = View.GONE
                    cardViewImage.visibility = View.VISIBLE
                    cardViewImageForSenderName.visibility = View.VISIBLE
                    lnImageTime.visibility = View.VISIBLE
                    imgMessage.loadImage(chatModel.url)

                    if (chatModel.status == Constants.TYPE_START_UPLOAD) {
                        progressBarImage.visibility = View.VISIBLE
                        progressBarImage.progress = time
                        context.uploadImageWithProgress(
                            chatModel.url.toString(),
                            chatModel,
                            progressBarImage
                        )
                        chatModel.status = Constants.TYPE_UPLOADING
                    } else {
                        progressBarImage.visibility = View.GONE
                    }
                }
                chatModel.messageType == Constants.TYPE_VIDEO -> {

                    llMessage.visibility = View.GONE
                    lnMessageTime.visibility = View.GONE
                    imgMessage.visibility = View.VISIBLE
                    imgPlay.visibility = View.VISIBLE
                    cardViewImage.visibility = View.VISIBLE
                    cardViewImageForSenderName.visibility = View.VISIBLE
                    lnImageTime.visibility = View.VISIBLE
                    imgMessage.loadImage(chatModel.url)

                    if (chatModel.status == Constants.TYPE_START_UPLOAD) {
                        progressBarImage.visibility = View.VISIBLE
                        progressBarImage.progress = time
                        context.uploadVideoWithProgress(
                            chatModel.url.toString(),
                            chatModel,
                            progressBarImage
                        )
                        chatModel.status = Constants.TYPE_UPLOADING
                    } else {
                        progressBarImage.visibility = View.GONE
                    }
                }
            }

            when (chatModel.status) {
                Constants.TYPE_READ -> {
                    imgMessageStatus.setImageResource(R.drawable.ic_read_dealer)
                    imgMessageStatus1.setImageResource(R.drawable.ic_read_dealer)
                }
                Constants.TYPE_SEND -> {
                    imgMessageStatus.setImageResource(R.drawable.ic_check_black)
                    imgMessageStatus1.setImageResource(R.drawable.ic_check_black)
                }
                Constants.TYPE_UPLOADING -> {
                    imgMessageStatus.setImageResource(R.drawable.ic_upload)
                    imgMessageStatus1.setImageResource(R.drawable.ic_upload)
                }
            }

            imgMessage.setOnClickListener {
                when (chatModel.messageType) {
                    Constants.TYPE_IMAGE -> {
                        context.redirectImageViewActivity(chatModel.url.toString())
                    }
                    else -> {
                        context.redirectVideoPlayActivity(chatModel.videoUrl.toString())
                    }
                }
            }
        }
    }
}
