package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.R

class ReceiverAvatarMessageView(itemView: View) {

    var userAvatar: LinearLayout? = null
    var time: TextView? = null

    init {
        time = itemView.findViewById<TextView>(R.id.messageTime) as TextView
        userAvatar = itemView.findViewById<LinearLayout>(R.id.userAvatar) as LinearLayout
    }

    fun onBind(message: Message) {
        userAvatar?.visibility = if (message.style.hideIncoming) View.GONE else View.VISIBLE

        if (userAvatar?.visibility == View.VISIBLE && userAvatar != null) {
            val isAvatarExists = ! ( message.user?.avatar?.isEmpty() ?: false)

            userAvatar!!.visibility = if (isAvatarExists) View.VISIBLE else View.GONE
        }
    }
}
