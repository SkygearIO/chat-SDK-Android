package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.TextView
import io.skygear.chatkit.utils.DateFormatter
import io.skygear.plugins.chat.ui.R
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.model.MessageStatusStyle
import io.skygear.plugins.chat.ui.model.MessageTimeStyle

open class BaseTimeMessageView(itemView: View){
    var timeTextView: TextView? = null
    var statusTextView: TextView? = null

    init {
        timeTextView = itemView.findViewById<TextView>(R.id.messageTime)
        statusTextView = itemView.findViewById<TextView>(R.id.messageStatus)
    }

    fun onBind(message: Message) {
        timeTextView?.setTextColor(textColor(message.style.timeStyle))
        timeTextView?.text = DateFormatter.format(message.getCreatedAt(), message.style.dateFormat)

        statusTextView?.setTextColor(statusTextColor(message.style.statusStyle))
        statusTextView?.text = message.getStatus()
    }

    open fun textColor(style: MessageTimeStyle): Int {
        return -1
    }

    open fun statusTextColor(style: MessageStatusStyle): Int {
        return -1
    }
}