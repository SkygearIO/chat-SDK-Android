package com.stfalcon.chatkit.messages
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ImageButton
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.commons.ViewHolder
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.utils.DateFormatter
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.ui.AvatarAdapter
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.DefaultAvatarAdapter
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.model.VoiceMessage


class CustomMessageHolders(avatarAdapterFunc: () -> AvatarAdapter, conversationFunc: () -> Conversation?): MessageHolders() {
    var avatarAdapterFunc: () -> AvatarAdapter = { DefaultAvatarAdapter()}
    var voiceMessageOnClickListener: VoiceMessageOnClickListener? = null
    var conversationFunc: () -> Conversation? = {null}
    init {
        this.avatarAdapterFunc = avatarAdapterFunc
        this.conversationFunc = conversationFunc
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
                    containerView.addView(avatarAdapterFunc()?.createAvatarView(inflater, containerView))
                }
                val avatarView = containerView.getChildAt(0)
                avatarAdapterFunc()?.bind(avatarView, conversationFunc(),item as Message, item.user)
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