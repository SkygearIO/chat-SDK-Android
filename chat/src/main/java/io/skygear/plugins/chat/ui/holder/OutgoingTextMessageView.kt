package io.skygear.plugins.chat.ui.holder

import android.view.View

import com.stfalcon.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.Message


class OutgoingTextMessageView(itemView: View) : MessageHolders.OutcomingTextMessageViewHolder<Message>(itemView) {

    var receiverAvatarMessageView: ReceiverAvatarMessageView? = null

    init {
        receiverAvatarMessageView = ReceiverAvatarMessageView(itemView)
    }

    override fun onBind(message: Message) {
        super.onBind(message)
        receiverAvatarMessageView?.onBind(message)
    }
}
