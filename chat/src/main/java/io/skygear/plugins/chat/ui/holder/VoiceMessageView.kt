package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.skygear.plugins.chat.ui.model.VoiceMessage


class VoiceMessageView(itemView: View){

    var actionButton: ImageView? = null
    var durationTextView: TextView? = null
    var timeTextView: TextView? = null

    init {
        this.actionButton = itemView.findViewById<ImageView>(io.skygear.plugins.chat.R.id.action_button)
        this.durationTextView = itemView.findViewById<TextView>(io.skygear.plugins.chat.R.id.duration)
        this.timeTextView = itemView.findViewById<TextView>(io.skygear.plugins.chat.R.id.messageTime)
    }

    fun onBind(message: VoiceMessage) {
        message?.let { msg ->
            val durationInSecond = msg.duration / 1000
            this@VoiceMessageView.durationTextView?.text =
                    String.format("%02d:%02d", durationInSecond / 60, durationInSecond % 60)

            this@VoiceMessageView.timeTextView?.setTextColor(com.stfalcon.chatkit.R.color.dark_gray)
            val actionButtonIcon = when (msg.state) {
                VoiceMessage.State.PLAYING -> io.skygear.plugins.chat.R.drawable.ic_pause_white
                else -> io.skygear.plugins.chat.R.drawable.ic_play_white
            }
            this@VoiceMessageView.actionButton?.setImageResource(actionButtonIcon)
        }
    }
}
