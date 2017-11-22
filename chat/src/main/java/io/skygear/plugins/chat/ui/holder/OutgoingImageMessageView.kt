package io.skygear.plugins.chat.ui.holder

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.ImageMessage


class OutgoingImageMessageView(itemView: View) : MessageHolders.OutcomingImageMessageViewHolder<ImageMessage>(itemView) {
    var receiverAvatarMessageView: ReceiverAvatarMessageView? = null
    var usernameMessageView: UsernameMessageView? = null

    init {
        receiverAvatarMessageView = ReceiverAvatarMessageView(itemView)
        usernameMessageView = UsernameMessageView(itemView)
    }

    override fun onBind(message: ImageMessage) {
        super.onBind(message)
        usernameMessageView?.onBind(message)
        receiverAvatarMessageView?.onBind(message)
    }
}
