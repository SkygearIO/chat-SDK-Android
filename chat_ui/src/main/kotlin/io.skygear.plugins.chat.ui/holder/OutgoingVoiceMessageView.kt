package io.skygear.plugins.chat.ui.holder

import android.view.View
import io.skygear.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.VoiceMessage

class OutgoingVoiceMessageView(view: View): MessageHolders.OutcomingTextMessageViewHolder<VoiceMessage>(view)
{
    var voiceMessageView: VoiceMessageView? = null
    var receiverAvatarMessageView: ReceiverAvatarMessageView? = null
    var timeMessageView: OutgoingTimeMessageView? = null
    var bubbleView: OutgoingBubbleMessageView? = null

    init {
        voiceMessageView = VoiceMessageView(itemView)
        timeMessageView = OutgoingTimeMessageView(itemView)
        receiverAvatarMessageView = ReceiverAvatarMessageView(itemView)
        bubbleView = OutgoingBubbleMessageView(itemView)
    }

    override fun onBind(message: VoiceMessage) {
        super.onBind(message)
        voiceMessageView?.onBind(message)
        receiverAvatarMessageView?.onBind(message)
        timeMessageView?.onBind(message)
        bubbleView?.onBind(message)
    }
}
