package io.skygear.plugins.chat.ui.holder

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.Message


class IncomingTextMessageView(itemView: View) : MessageHolders.IncomingTextMessageViewHolder<Message>(itemView) {

    var senderAvatarMessageView: SenderAvatarMessageView? = null
    var usernameMessageView: UsernameMessageView? = null

    init {
        usernameMessageView = UsernameMessageView(itemView)
        senderAvatarMessageView = SenderAvatarMessageView(itemView)

    }

    override fun onBind(message: Message) {
        super.onBind(message)
        usernameMessageView?.onBind(message)
        senderAvatarMessageView?.onBind(message)
    }
}
