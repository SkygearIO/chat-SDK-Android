package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.LinearLayout
import io.skygear.plugins.chat.ui.R
import io.skygear.plugins.chat.ui.model.Message

class SenderAvatarMessageView(itemView: View) {

    var avatarView: LinearLayout? = null

    init {
        avatarView = itemView.findViewById<LinearLayout>(R.id.userAvatar)
    }

    fun onBind(message: Message) {
        avatarView?.visibility = if (message.style.hideOutgoing) View.GONE else View.VISIBLE
    }
}
