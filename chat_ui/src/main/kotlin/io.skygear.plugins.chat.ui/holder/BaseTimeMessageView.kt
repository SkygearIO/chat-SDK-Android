package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.TextView
import io.skygear.plugins.chat.ui.R
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.model.MessageTimeStyle

open class BaseTimeMessageView(itemView: View){
    var timeTextView: TextView? = null

    init {
        timeTextView = itemView.findViewById<TextView>(R.id.messageTime)
    }

    fun onBind(message: Message) {
        timeTextView?.setTextColor(textColor(message.style.timeStyle))
    }

    open fun textColor(style: MessageTimeStyle): Int {
        return -1
    }
}