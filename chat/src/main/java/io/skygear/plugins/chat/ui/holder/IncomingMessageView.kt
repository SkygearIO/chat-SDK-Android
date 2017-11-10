package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.ImageView
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.Message

open class OutgoingMessageView<MESSAGE : IMessage>: MessageHolders.OutcomingTextMessageViewHolder<MESSAGE> {

    var userAvatar: ImageView? = null

    constructor(itemView: View): super(itemView) {
        userAvatar = itemView.findViewById<View>(com.stfalcon.chatkit.R.id.messageUserAvatar) as ImageView
    }

    override fun onBind(message: MESSAGE) {
        super.onBind(message)
        if (userAvatar != null) {
            val isAvatarExists = imageLoader != null
                    && message.user?.avatar != null
                    && ! (message.user?.avatar?.isEmpty() ?: false)
            userAvatar?.visibility = if (isAvatarExists) View.VISIBLE else View.GONE
            if (isAvatarExists) {
                imageLoader.loadImage(userAvatar, message.user?.avatar)
            }
        }
    }
}
