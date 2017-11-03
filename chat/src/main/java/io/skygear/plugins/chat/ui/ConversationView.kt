package io.skygear.plugins.chat.ui


import android.content.Context
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.dewarder.holdinglibrary.HoldingButtonLayout
import com.dewarder.holdinglibrary.HoldingButtonLayoutListener
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.stfalcon.chatkit.messages.MessagesList
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.holder.IncomingImageMessageView
import io.skygear.plugins.chat.ui.holder.IncomingTextMessageView
import io.skygear.plugins.chat.ui.holder.OutgoingImageMessageView
import io.skygear.plugins.chat.ui.holder.OutgoingTextMessageView
import io.skygear.plugins.chat.ui.utils.ImageLoader
import android.app.Activity
import com.stfalcon.chatkit.messages.MessagesListAdapter.OnMessageClickListener
import com.stfalcon.chatkit.messages.MessagesListAdapter.OnLoadMoreListener
import io.skygear.plugins.chat.ui.model.*
import io.skygear.plugins.chat.ui.utils.AvatarBuilder
import io.skygear.plugins.chat.Message as ChatMessage
import io.skygear.plugins.chat.ui.utils.UserCache
import io.skygear.skygear.Container



open abstract class HoldingButtonLayoutBaseListener : HoldingButtonLayoutListener {
    override fun onBeforeCollapse() {}

    override fun onOffsetChanged(offset: Float, isCancel: Boolean) {}

    override fun onBeforeExpand() {}

    override fun onExpand() {}

    override fun onCollapse(isCancel: Boolean) {}
}

open abstract class TextBaseWatcher : TextWatcher {
    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}

private class MessagesListViewReachBottomListener(
        private val layoutManager: LinearLayoutManager
) : RecyclerView.OnScrollListener() {
    companion object {
        private val BOTTOM_ITEM_THRESHOLD = 5
    }
    var isReachEnd: Boolean = false
        private set

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        val pastVisibleItems = this.layoutManager.findFirstVisibleItemPosition()
        this.isReachEnd = pastVisibleItems < BOTTOM_ITEM_THRESHOLD
    }
}

enum class AvatarType(val value: Int) {
    INITIAL(0),
    IMAGE(1);

    companion object {
        fun fromInt(value: Int): AvatarType {
            values().forEach { type ->
                if (type.value == value) {
                    return type
                }
            }
            throw IllegalArgumentException()
        }

    }
}


open class ConversationView: RelativeLayout{
    private var messagesListView: MessagesList? = null
    private var addAttachmentButton: ImageButton? = null
    private var messageSendButton: ImageButton? = null
    private var messageEditText: EditText? = null
    private var voiceButtonHolderHint: View? = null
    private var voiceButtonHolder: HoldingButtonLayout? = null
    private var progressBar: ProgressBar? = null
    private var messagesListViewReachBottomListener: MessagesListViewReachBottomListener? = null
    private var sendTextMessageListener: (String) -> Boolean? = {true}
    private var messageListAdapter: MessagesListAdapter<Message>? = null
    private var skygear: Container? = null
    private var userCache: UserCache? = null

    private var senderUserNameTextColor: Int
    private var avatarNameField: String
    private var avatarImageField: String
    private var avatarShowSender: Boolean
    private var avatarShowReceiver: Boolean
    private var avatarInitialTextColor: Int
    private var avatarBackgroundColor: Int
    private var avatarType: AvatarType
    private var userBuilder: UserBuilder




    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        val a = context.theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.ConversationView,
                0, 0);

       try {
           avatarNameField = a.getString(R.styleable.ConversationView_avatarNameField) ?: User.DefaultUsernameField
           avatarImageField = a.getString(R.styleable.ConversationView_avatarImageField) ?: User.DefaultAvatarField
           avatarShowSender = a.getBoolean(R.styleable.ConversationView_avatarShowSender, true)
           avatarShowReceiver = a.getBoolean(R.styleable.ConversationView_avatarShowReceiver, false)
           senderUserNameTextColor = a.getColor(R.styleable.ConversationView_senderUserNameTextColor, Color.BLACK)
           avatarType = AvatarType.fromInt(a.getInt(R.styleable.ConversationView_avatarType, AvatarType.INITIAL.value))
           avatarInitialTextColor = a.getColor(R.styleable.ConversationView_avatarInitialTextColor, Color.WHITE)
           avatarBackgroundColor = a.getColor(R.styleable.ConversationView_avatarBackgroundColor, Color.BLUE)
       } finally {
           a.recycle()
       }

        this.userBuilder = UserBuilder(avatarNameField, avatarImageField, avatarType, avatarBackgroundColor , avatarInitialTextColor)

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        inflater.inflate(R.layout.conversation_view, this, true)

        this.messagesListView = findViewById<MessagesList>(R.id.messages_list)
        this.addAttachmentButton = findViewById<ImageButton>(R.id.add_attachment_btn)
        this.messageSendButton = findViewById<ImageButton>(R.id.msg_send_btn)
        this.messageEditText = findViewById<EditText>(R.id.msg_edit_text)

        this.voiceButtonHolderHint = findViewById(R.id.voice_recording_btn_holder_hint)
        this.voiceButtonHolder = findViewById<HoldingButtonLayout>(R.id.voice_recording_btn_holder)


        this.progressBar = findViewById<ProgressBar>(R.id.progressBar)



        val conversationView = this
        this.messageEditText?.addTextChangedListener(object : TextBaseWatcher() {
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                conversationView.messageEditText?.text?.let { msgContent ->
                    if (msgContent.isEmpty()) {
                        conversationView.voiceButtonHolder?.visibility = View.VISIBLE
                        conversationView.messageSendButton?.visibility = View.INVISIBLE
                    } else {
                        conversationView.voiceButtonHolder?.visibility = View.INVISIBLE
                        conversationView.messageSendButton?.visibility = View.VISIBLE
                    }
                }
            }
        })

        this.messageSendButton?.setOnClickListener {
            _ ->
                messageEditText?.text?.let { text ->
                    if (!text.isEmpty()) {
                        val result = this?.sendTextMessageListener?.invoke(text.toString())
                        result?.let { success ->
                            if (success){
                                this.messageEditText?.setText("")
                            }
                        }
                    }
                }
        }
        this.skygear = Container.defaultContainer(context)
        this.messageListAdapter = createMessageListAdapter(ImageLoader(context as Activity, AvatarBuilder()), this.skygear?.auth?.currentUser!!.id)
        this.messagesListView?.setAdapter(this.messageListAdapter)

        if (this.messagesListView?.layoutManager is LinearLayoutManager) {
            this.messagesListViewReachBottomListener = MessagesListViewReachBottomListener(
                    this.messagesListView?.layoutManager as LinearLayoutManager
            )
            (this.messagesListView?.layoutManager as LinearLayoutManager).isAutoMeasureEnabled = false
            this.messagesListView?.addOnScrollListener(this.messagesListViewReachBottomListener)
        }

        this.userCache = UserCache.getInstance(this.skygear!!, this.userBuilder)
    }

    open fun setAddAttachmentButtonOnClickListener(action: (View) -> Unit) {
        this.addAttachmentButton?.setOnClickListener(action)
    }

    open fun setVoiceButtonHolderListener(listener: HoldingButtonLayoutBaseListener) {
        this.voiceButtonHolder?.addListener(listener)
    }

    open fun setSendTextMessageListener(action: (String) -> Boolean) {
        this.sendTextMessageListener = action
    }

    open fun needToScrollToBottom(): Boolean {
        return this.messagesListViewReachBottomListener?.isReachEnd !!;
    }

    fun createMessageListAdapter(imageLoader: ImageLoader, senderId: String): MessagesListAdapter<Message> {
        val messageHolder = MessageHolders()
                .setIncomingTextHolder(IncomingTextMessageView::class.java)
                .setIncomingImageHolder(IncomingImageMessageView::class.java)
                .setIncomingTextLayout(R.layout.item_incoming_text_message)
                .setIncomingImageLayout(R.layout.item_incoming_image_message)
                .setOutcomingTextHolder(OutgoingTextMessageView::class.java)
                .setOutcomingImageHolder(OutgoingImageMessageView::class.java)
                .setOutcomingImageLayout(R.layout.item_outgoing_image_message)
                .setOutcomingTextLayout(R.layout.item_outgoing_text_message)
                .registerContentType(
                        ContentTypeChecker.VoiceMessageType,
                        IncomingVoiceMessageView::class.java, R.layout.item_incoming_voice_message,
                        OutgoingVoiceMessageView::class.java, R.layout.item_outgoing_voice_message,
                        ContentTypeChecker()
                )
        val adapter = MessagesListAdapter<Message>(
                senderId,
                messageHolder,
                imageLoader
        )
        return adapter
    }

    open fun setOnMessageClickListener(onMessageClickListener: OnMessageClickListener<Message>) {
        this.messageListAdapter?.setOnMessageClickListener(onMessageClickListener)
    }

    open fun setLoadMoreListener(onLoadMoreListener: OnLoadMoreListener) {
        this.messageListAdapter?.setLoadMoreListener(onLoadMoreListener)
    }

    open fun itemCount(): Int? {
        return this.messageListAdapter?.itemCount
    }

    open fun hideProgress() {
        this.progressBar?.visibility = View.GONE
    }

    open fun toggleVoiceButtonHint(flag: Boolean) {
        this.voiceButtonHolderHint?.visibility = if (flag) View.VISIBLE else View.INVISIBLE
        listOf(this.addAttachmentButton, this.messageEditText).map {
            it?.visibility = if (! flag) View.VISIBLE else View.INVISIBLE
        }
    }

    open fun cancelVoiceButton() {
        this.voiceButtonHolder?.cancel()
    }

    open fun updateMessage(message: ChatMessage) {
        MessagesFromChatMessages(listOf(message)) {
            msgs -> this.messageListAdapter?.update(msgs.first())
        }
    }

    open fun addMessagesToEnd(messages: List<ChatMessage>, reverse: Boolean) {
        MessagesFromChatMessages(messages) {
            msgs -> this.messageListAdapter?.addToEnd(msgs, reverse)
        }
    }

    open fun addMessageToStart(message: ChatMessage, scroll: Boolean) {
        MessagesFromChatMessages(listOf(message)) {
            msgs -> this.messageListAdapter?.addToStart(msgs.first(), scroll)
        }
    }

    fun getMessageStyle(): MessageStyle {
        return MessageStyle(this.avatarShowSender, this.avatarShowReceiver, this.senderUserNameTextColor)
    }

    fun MessagesFromChatMessages(chatMessages: List<ChatMessage>, callback: ((messages: List<Message>) -> Unit)? ) {
        val userIDs = chatMessages.map { it.record.ownerId }.distinct()
        val multitypeMessages = chatMessages.map { MessageFactory.getMessage(it, getMessageStyle()) }
        this.userCache?.getUsers(userIDs) { userIDs ->
            multitypeMessages.forEach {
                msg ->
                    val ownerId = msg.chatMessage.record.ownerId
                    if (ownerId != null && userIDs.containsKey(ownerId)) {
                        msg.author = userIDs[ownerId]
                    } else {
                        msg.author = userBuilder.createUser(this.skygear?.auth?.currentUser!!)
                    }
            }
            callback?.invoke(multitypeMessages)
        }
    }

    class ContentTypeChecker : MessageHolders.ContentChecker<Message> {
        companion object {
            val VoiceMessageType: Byte = 1
        }

        override fun hasContentFor(message: Message?, type: Byte): Boolean {
            if (message == null) return false

            return when (type) {
                ContentTypeChecker.VoiceMessageType -> VoiceMessage.isVoiceMessage(message)
                else -> false
            }
        }
    }
}