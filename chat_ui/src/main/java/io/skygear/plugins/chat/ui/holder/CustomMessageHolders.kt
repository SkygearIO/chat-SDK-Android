package io.skygear.chatkit.messages
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ImageButton
import io.skygear.chatkit.commons.ImageLoader
import io.skygear.chatkit.commons.ViewHolder
import io.skygear.chatkit.commons.models.IMessage
import io.skygear.chatkit.utils.DateFormatter
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.ui.AvatarAdapter
import io.skygear.plugins.chat.ui.R
import io.skygear.plugins.chat.ui.DefaultAvatarAdapter
import io.skygear.plugins.chat.ui.model.ImageMessage
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.model.VoiceMessage

class CustomMessageHolders(avatarAdapterFunc: () -> AvatarAdapter, conversationFunc: () -> Conversation?) : MessageHolders() {
    // Copied from chatkit project
    val VIEW_TYPE_IMAGE_MESSAGE = 132
    var avatarAdapterFunc: () -> AvatarAdapter = { DefaultAvatarAdapter() }
    var voiceMessageOnClickListener: VoiceMessageOnClickListener? = null
    var conversationFunc: () -> Conversation? = { null }
    init {
        this.avatarAdapterFunc = avatarAdapterFunc
        this.conversationFunc = conversationFunc
    }

    override fun getViewType(item: Any, senderId: String): Int {
        // getContentViewType from MessageHolders.java can only recognize ImageMessage with URL, however, failed ImageMessage does not have URL,
        // since failed Image does not have a valid URL.
        if (item is ImageMessage) {
            val isOutcoming = item.user?.id?.contentEquals(senderId) ?: false
            return (if (isOutcoming) -1 else 1) * VIEW_TYPE_IMAGE_MESSAGE
        } else {
            return super.getViewType(item, senderId)
        }
    }

    /*
        bind() is to assign listeners parameters to holder.itemView and its children.
        Override bind() and set voiceMessageOnClickListener to action_button
        Source: https://github.com/stfalcon-studio/ChatKit/blob/master/chatkit/src/main/java/com/stfalcon/chatkit/messages/MessageHolders.java#L351
    */
    override fun bind(holder: ViewHolder<*>?,
                      item: Any?,
                      isSelected: Boolean,
                      imageLoader: ImageLoader?,
                      onMessageClickListener: View.OnClickListener?,
                      onMessageLongClickListener: View.OnLongClickListener?,
                      dateHeadersFormatter: DateFormatter.Formatter?,
                      clickListenersArray: SparseArray<MessagesListAdapter.OnMessageViewClickListener<IMessage>>?) {
        super.bind(holder, item, isSelected, imageLoader, onMessageClickListener, onMessageLongClickListener, dateHeadersFormatter, clickListenersArray)
        val inflater = LayoutInflater.from(holder?.itemView?.context)
        val avatarContainerView = holder?.itemView?.findViewById<LinearLayout>(R.id.userAvatar)
        avatarContainerView?.let { containerView ->
            if (containerView.visibility == View.VISIBLE) {
                if (containerView.childCount == 0) {
                    containerView.addView(avatarAdapterFunc().createAvatarView(inflater, containerView))
                }
                val avatarView = containerView.getChildAt(0)
                avatarAdapterFunc().bind(avatarView, conversationFunc(), item as Message, item.user)
            }
        }

        if (item is VoiceMessage) {
            val button = holder?.itemView?.findViewById<ImageButton>(R.id.action_button)
            button?.setOnClickListener { _ ->
                this.voiceMessageOnClickListener?.onVoiceMessageClick(item)
            }
        }
    }
}