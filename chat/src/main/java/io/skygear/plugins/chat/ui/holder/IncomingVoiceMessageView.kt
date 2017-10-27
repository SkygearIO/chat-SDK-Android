package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.utils.DateFormatter
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.model.VoiceMessage

class IncomingVoiceMessageView(view: View):
        MessageHolders.IncomingTextMessageViewHolder<VoiceMessage>(view)
{
    var actionButton: ImageView? = null
    var durationTextView: TextView? = null
    var timeTextView: TextView? = null

    init {
        this.actionButton = view.findViewById<ImageView>(R.id.action_button)
        this.durationTextView = view.findViewById<TextView>(R.id.duration)
        this.timeTextView = view.findViewById<TextView>(R.id.time)
    }

    override fun onBind(message: VoiceMessage?) {
        super.onBind(message)
        message?.let { msg ->
            val durationInSecond = msg.duration / 1000
            this@IncomingVoiceMessageView.durationTextView?.text =
                    String.format("%02d:%02d", durationInSecond / 60, durationInSecond % 60)

            this@IncomingVoiceMessageView.timeTextView?.text =
                    DateFormatter.format(msg.createdAt, DateFormatter.Template.TIME)

            val actionButtonIcon = when(msg.state) {
                VoiceMessage.State.PLAYING -> R.drawable.ic_pause
                else -> R.drawable.ic_play
            }
            this@IncomingVoiceMessageView.actionButton?.setImageResource(actionButtonIcon)
        }
    }
}
