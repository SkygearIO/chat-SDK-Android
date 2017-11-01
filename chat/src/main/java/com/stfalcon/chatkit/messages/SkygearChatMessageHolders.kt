package com.stfalcon.chatkit.messages

import android.util.SparseArray
import android.view.View
import android.widget.ImageButton
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.commons.ViewHolder
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.utils.DateFormatter
import io.skygear.plugins.chat.ui.model.VoiceMessage
import io.skygear.plugins.chat.R

class SkygearChatMessageHolders : MessageHolders {
    var voiceMessageOnClickListener: VoiceMessageOnClickListener?
    constructor(voiceMessageOnClickListener: VoiceMessageOnClickListener?): super() {
        this.voiceMessageOnClickListener = voiceMessageOnClickListener
    }

    override fun bind(holder: ViewHolder<*>?,
                      item: Any?,
                      isSelected: Boolean,
                      imageLoader: ImageLoader?,
                      onMessageClickListener: View.OnClickListener?,
                      onMessageLongClickListener: View.OnLongClickListener?,
                      dateHeadersFormatter: DateFormatter.Formatter?,
                      clickListenersArray: SparseArray<MessagesListAdapter.OnMessageViewClickListener<IMessage>>?) {

        /*
            bind() is to assign listeners parameters to holder.itemView and its children.
            Override bind() and set voiceMessageOnClickListener to action_button
            Source: https://github.com/stfalcon-studio/ChatKit/blob/master/chatkit/src/main/java/com/stfalcon/chatkit/messages/MessageHolders.java#L351
         */
        super.bind(holder, item, isSelected, imageLoader, onMessageClickListener, onMessageLongClickListener, dateHeadersFormatter, clickListenersArray)
        if (item is VoiceMessage) {
            val button = holder?.itemView?.findViewById<ImageButton>(R.id.action_button)
            button?.setOnClickListener { _ ->
                this.voiceMessageOnClickListener?.onClick(item)
            }
        }
    }
}
