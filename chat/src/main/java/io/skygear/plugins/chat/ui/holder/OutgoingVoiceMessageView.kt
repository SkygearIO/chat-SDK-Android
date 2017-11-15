package io.skygear.plugins.chat.ui.holder

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.VoiceMessage

class OutgoingVoiceMessageView(view: View): MessageHolders.OutcomingTextMessageViewHolder<VoiceMessage>(view)
{
    var voiceMessageView: VoiceMessageView? = null
    var receiverAvatarMessageView: ReceiverAvatarMessageView? = null

    init {
        voiceMessageView = VoiceMessageView(itemView)
        receiverAvatarMessageView = ReceiverAvatarMessageView(itemView)
    }

    override fun onBind(message: VoiceMessage) {
        super.onBind(message)
        voiceMessageView?.onBind(message)
        receiverAvatarMessageView?.onBind(message, this.imageLoader)
    }
}
