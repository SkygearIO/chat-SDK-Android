package io.skygear.plugins.chat.ui.holder

import android.view.View
import io.skygear.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.ImageMessage


class IncomingImageMessageView(itemView: View) : MessageHolders.IncomingImageMessageViewHolder<ImageMessage>(itemView) {

    var usernameMessageView: UsernameMessageView? = null

    init {
        usernameMessageView = UsernameMessageView(itemView)
    }

    override fun onBind(message: ImageMessage) {
        super.onBind(message)
        usernameMessageView?.onBind(message)
    }
}
