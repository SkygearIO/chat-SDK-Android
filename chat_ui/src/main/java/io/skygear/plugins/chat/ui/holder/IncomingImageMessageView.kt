package io.skygear.plugins.chat.ui.holder

import android.view.View
import io.skygear.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.model.ImageMessage
import io.skygear.plugins.chat.ui.utils.ImageLoader

class IncomingImageMessageView(itemView: View) : MessageHolders.IncomingImageMessageViewHolder<ImageMessage>(itemView) {

    var senderAvatarMessageView: SenderAvatarMessageView? = null
    var usernameMessageView: UsernameMessageView? = null
    var timeMessageView: IncomingTimeMessageView? = null
    init {
        usernameMessageView = UsernameMessageView(itemView)
        timeMessageView = IncomingTimeMessageView(itemView)
        senderAvatarMessageView = SenderAvatarMessageView(itemView)
    }

    override fun onBind(message: ImageMessage) {
        super.onBind(message)
        val loader = imageLoader
        if (loader is ImageLoader && image != null) {
            loader.loadImage(image, message.chatMessageImageUrl, message.thumbnail,
                    message.width, message.height, message.orientation)
        }

        usernameMessageView?.onBind(message)
        timeMessageView?.onBind(message)
        senderAvatarMessageView?.onBind(message)
    }
}
