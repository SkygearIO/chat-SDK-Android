package io.skygear.plugins.chat.ui.holder

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.ImageMessage


/**
 * Created by carmenlau on 10/13/17.
 */
class CustomOutcomingImageMessageViewHolder(itemView: View) : MessageHolders.OutcomingImageMessageViewHolder<ImageMessage>(itemView) {

    override fun onBind(message: ImageMessage) {
        super.onBind(message)

        time.setText(message.getStatus() + " " + time.text)
    }
}
