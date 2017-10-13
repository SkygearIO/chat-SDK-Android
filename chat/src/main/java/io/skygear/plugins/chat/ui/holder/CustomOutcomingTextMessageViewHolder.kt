package io.skygear.plugins.chat.ui.holder

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.Message


/**
 * Created by carmenlau on 10/13/17.
 */
class CustomOutcomingTextMessageViewHolder(itemView: View) : MessageHolders.OutcomingTextMessageViewHolder<Message>(itemView) {

    override fun onBind(message: Message) {
        super.onBind(message)

        time.setText(message.getStatus() + " " + time.text)
    }
}
