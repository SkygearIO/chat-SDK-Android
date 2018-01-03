package io.skygear.plugins.chat.ui.holder

import android.view.View
import io.skygear.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.VoiceMessage

class IncomingVoiceMessageView(view: View):
        MessageHolders.IncomingTextMessageViewHolder<VoiceMessage>(view)
{
    var voiceMessageView: VoiceMessageView? = null
    var usernameMessageView: UsernameMessageView? = null
    var senderAvatarMessageView: SenderAvatarMessageView? = null
    var timeMessageView: IncomingTimeMessageView? = null
    var bubbleView: IncomingBubbleMessageView? = null

    init {
        voiceMessageView = VoiceMessageView(itemView)
        usernameMessageView = UsernameMessageView(itemView)
        senderAvatarMessageView = SenderAvatarMessageView(itemView)
        bubbleView = IncomingBubbleMessageView(itemView)
    }

    override fun onBind(message: VoiceMessage) {
        super.onBind(message)
        voiceMessageView?.onBind(message)
        usernameMessageView?.onBind(message)
        senderAvatarMessageView?.onBind(message)
        timeMessageView?.onBind(message)
        bubbleView?.onBind(message)

    }
}
