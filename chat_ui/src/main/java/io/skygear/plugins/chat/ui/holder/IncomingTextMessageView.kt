package io.skygear.plugins.chat.ui.holder

import android.view.View
import io.skygear.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.Message

class IncomingTextMessageView(itemView: View) : MessageHolders.IncomingTextMessageViewHolder<Message>(itemView) {

    var senderAvatarMessageView: SenderAvatarMessageView? = null
    var usernameMessageView: UsernameMessageView? = null
    var timeMessageView: IncomingTimeMessageView? = null
    var bubbleView: IncomingBubbleMessageView? = null

    init {
        usernameMessageView = UsernameMessageView(itemView)
        senderAvatarMessageView = SenderAvatarMessageView(itemView)
        timeMessageView = IncomingTimeMessageView(itemView)
        bubbleView = IncomingBubbleMessageView(itemView)
    }

    override fun onBind(message: Message) {
        bubble?.isSelected = isSelected
        text?.text = message.text

        usernameMessageView?.onBind(message)
        senderAvatarMessageView?.onBind(message)
        timeMessageView?.onBind(message)
        bubbleView?.onBind(message)
    }
}
