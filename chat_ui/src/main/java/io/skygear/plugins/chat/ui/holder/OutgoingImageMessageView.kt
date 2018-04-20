package io.skygear.plugins.chat.ui.holder

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import io.skygear.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.ui.R
import io.skygear.plugins.chat.ui.model.ImageMessage

class OutgoingImageMessageView(itemView: View) : MessageHolders.OutcomingImageMessageViewHolder<ImageMessage>(itemView) {
    var receiverAvatarMessageView: ReceiverAvatarMessageView? = null
    var usernameMessageView: UsernameMessageView? = null
    var timeMessageView: OutgoingTimeMessageView? = null
    var imageView: ImageView? = null

    init {
        receiverAvatarMessageView = ReceiverAvatarMessageView(itemView)
        usernameMessageView = UsernameMessageView(itemView)
        timeMessageView = OutgoingTimeMessageView(itemView)
        imageView = itemView.findViewById(R.id.image)
    }

    override fun onBind(message: ImageMessage) {
        super.onBind(message)
        if (message.chatMessage.asset != null && message.imageUrl == null) {
            message.chatMessage.asset?.data?.let { data ->
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                imageView?.setImageBitmap(bitmap)
            }
        }
        usernameMessageView?.onBind(message)
        receiverAvatarMessageView?.onBind(message)
        timeMessageView?.onBind(message)
    }
}
