package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.R
import io.skygear.plugins.chat.ui.model.Message


class ReceiverAvatarMessageView(itemView: View){

    var userAvatar: ImageView? = null
    var time: TextView? = null

    init {
        userAvatar = itemView.findViewById<View>(R.id.messageUserAvatar) as ImageView
        time = itemView.findViewById<TextView>(R.id.messageTime) as TextView
    }

    fun onBind(message: Message, imageLoader: ImageLoader) {
        userAvatar?.visibility = if (message.style.showReceiver) View.VISIBLE else View.GONE

        if (userAvatar?.visibility == View.VISIBLE && userAvatar != null) {
            val isAvatarExists = imageLoader != null
                    && ! ( message.user?.avatar?.isEmpty() ?: false)

            userAvatar!!.visibility = if (isAvatarExists) View.VISIBLE else View.GONE
            if (isAvatarExists) {
                imageLoader.loadImage(userAvatar, message.user?.avatar)
            }
        }

        time?.text = message.getStatus()

    }
}
