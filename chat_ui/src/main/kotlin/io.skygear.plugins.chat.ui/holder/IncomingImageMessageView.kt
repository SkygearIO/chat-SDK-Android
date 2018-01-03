package io.skygear.plugins.chat.ui.holder

import android.view.View
import io.skygear.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.ImageMessage


class IncomingImageMessageView(itemView: View) : MessageHolders.IncomingTextMessageViewHolder<ImageMessage>(itemView) {

    var usernameMessageView: UsernameMessageView? = null
    var timeMessageView: IncomingTimeMessageView? = null
    init {
        usernameMessageView = UsernameMessageView(itemView)
        timeMessageView = IncomingTimeMessageView(itemView)
    }

    override fun onBind(message: ImageMessage) {
        super.onBind(message)
        usernameMessageView?.onBind(message)
        timeMessageView?.onBind(message)
    }
}
