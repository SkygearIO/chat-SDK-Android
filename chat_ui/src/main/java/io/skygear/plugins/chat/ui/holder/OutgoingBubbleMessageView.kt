package io.skygear.plugins.chat.ui.holder

import android.view.View
import io.skygear.chatkit.R
import io.skygear.plugins.chat.ui.model.MessageBubbleStyle

class OutgoingBubbleMessageView(itemView: View) : BaseBubbleMessageView(itemView, R.drawable.shape_outcoming_message, false) {
    override fun backgroundColor(style: MessageBubbleStyle): Int {
        return style.backgroundColorForOutgoingMessages
    }
}