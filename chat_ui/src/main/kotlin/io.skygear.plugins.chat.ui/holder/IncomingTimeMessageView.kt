package io.skygear.plugins.chat.ui.holder

import android.view.View
import io.skygear.plugins.chat.ui.model.MessageTimeStyle

class IncomingTimeMessageView(itemView: View): BaseTimeMessageView(itemView) {
    override fun textColor(style: MessageTimeStyle): Int {
        return style.incomingTextColor
    }
}