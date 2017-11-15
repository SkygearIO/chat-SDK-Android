package io.skygear.plugins.chat.ui.holder

import android.view.View
import com.stfalcon.chatkit.utils.ShapeImageView
import io.skygear.plugins.chat.ui.model.Message


class SenderAvatarMessageView(itemView: View){

    var avatarImageView: ShapeImageView? = null

    init {
        avatarImageView = itemView.findViewById<ShapeImageView>(io.skygear.plugins.chat.R.id.messageUserAvatar)
    }

    fun onBind(message: Message) {
        avatarImageView?.visibility = if (message.style.showSender) View.VISIBLE else View.GONE
    }
}
