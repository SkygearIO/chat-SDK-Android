package io.skygear.plugins.chat.ui


import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.dewarder.holdinglibrary.HoldingButtonLayout
import com.dewarder.holdinglibrary.HoldingButtonLayoutListener
import io.skygear.plugins.chat.R
import android.app.Activity
import android.net.Uri
import android.widget.*
import io.skygear.plugins.chat.ui.holder.*
import io.skygear.plugins.chat.ui.model.*
import io.skygear.plugins.chat.ui.utils.AvatarBuilder
import io.skygear.plugins.chat.Message as ChatMessage
import io.skygear.skygear.Container
import io.skygear.chatkit.messages.CustomMessageHolders
import io.skygear.plugins.chat.Conversation
import io.skygear.chatkit.messages.VoiceMessageOnClickListener
import io.skygear.plugins.chat.ui.utils.ImageLoader
import io.skygear.chatkit.messages.MessageHolders
import io.skygear.chatkit.messages.MessagesList
import io.skygear.chatkit.messages.MessagesListAdapter
import io.skygear.skygear.Record


abstract class HoldingButtonLayoutBaseListener : HoldingButtonLayoutListener {
    override fun onBeforeCollapse() {}

    override fun onOffsetChanged(offset: Float, isCancel: Boolean) {}

    override fun onBeforeExpand() {}

    override fun onExpand() {}

    override fun onCollapse(isCancel: Boolean) {}
}

abstract class TextBaseWatcher : TextWatcher {
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
    private var messageListAdapter: SortedMessageListAdapter? = null
    private var skygear: Container? = null

    private var messageSenderTextColor: Int
    private var avatarNameField: String
    private var avatarImageField: String
    private var avatarHiddenForOutgoingMessages: Boolean
    private var avatarHiddenForIncomingMessages: Boolean
    private var avatarTextColor: Int
    private var avatarBackgroundColor: Int
    private var userAvatarType: AvatarType
    private var userBuilder: UserBuilder
    private var avatarAdapter: AvatarAdapter = DefaultAvatarAdapter()
    private var conversation: Conversation? = null
    private var messageHolders: MessageHolders? = null

    private var userMap: MutableMap<String, User> = mutableMapOf()


    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        val a = context.theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.ConversationView,
                0, 0)

        try {
            avatarNameField = a.getString(R.styleable.ConversationView_userNameField) ?: User.DefaultUsernameField
            avatarImageField = a.getString(R.styleable.ConversationView_userAvatarField) ?: User.DefaultAvatarField
            avatarHiddenForOutgoingMessages = a.getBoolean(R.styleable.ConversationView_avatarHiddenForOutgoingMessages, false)
            avatarHiddenForIncomingMessages = a.getBoolean(R.styleable.ConversationView_avatarHiddenForIncomingMessages, true)
            messageSenderTextColor = a.getColor(R.styleable.ConversationView_messageSenderTextColor, Color.BLACK)
            userAvatarType = AvatarType.fromInt(a.getInt(R.styleable.ConversationView_userAvatarType, AvatarType.INITIAL.value))
            avatarTextColor = a.getColor(R.styleable.ConversationView_avatarTextColor, Color.WHITE)
            avatarBackgroundColor = a.getColor(R.styleable.ConversationView_avatarBackgroundColor, ContextCompat.getColor(context, R.color.blue_1))
        } finally {
            a.recycle()
        }

        this.userBuilder = UserBuilder(avatarNameField, avatarImageField, userAvatarType, avatarBackgroundColor , avatarTextColor)

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
                        val result = this.sendTextMessageListener.invoke(text.toString())
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

    open fun setVoiceMessageOnClickListener(listener: VoiceMessageOnClickListener) {
        (this.messageHolders as CustomMessageHolders).voiceMessageOnClickListener = listener
    }

    open fun needToScrollToBottom(): Boolean {
        return this.messagesListViewReachBottomListener?.isReachEnd !!;
    }

    fun createMessageListAdapter(imageLoader: ImageLoader, senderId: String): SortedMessageListAdapter {
        messageHolders = CustomMessageHolders({avatarAdapter}, {conversation})
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

        val adapter = SortedMessageListAdapter(
                senderId,
                messageHolders!!,
                imageLoader
        )
        return adapter
    }

    open fun setAvatarAdapter(newAdapter: AvatarAdapter) {
        avatarAdapter = newAdapter
    }

    open fun setConversation(newConversation: Conversation?) {
        conversation = newConversation
    }

    open fun setOnMessageClickListener(onMessageClickListener: MessagesListAdapter.OnMessageClickListener<Message>) {
        this.messageListAdapter?.setOnMessageClickListener(onMessageClickListener)
    }

    open fun setOnMessageLongClickListener(onMessageLongClickListener: MessagesListAdapter.OnMessageLongClickListener<Message>) {
        this.messageListAdapter?.setOnMessageLongClickListener(onMessageLongClickListener)
    }

    open fun setLoadMoreListener(onLoadMoreListener: MessagesListAdapter.OnLoadMoreListener) {
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
        this.messageListAdapter?.update(MessagesFromChatMessages(listOf(message)).first())
    }

    open fun updateVoiceMessage(voiceMessage: VoiceMessage) {
        updateMessageAuthor(voiceMessage)
        this.messageListAdapter?.update(voiceMessage)
    }

    open fun mergeMessagesToList(messages: List<ChatMessage>) {
        this.messageListAdapter?.merge(MessagesFromChatMessages(messages))
    }

    open fun addMessageToBottom(message: ChatMessage, imageUri: Uri? = null) {
        this.messageListAdapter?.addToStart(MessageFromChatMessage(message, imageUri), needToScrollToBottom())
    }

    open fun startListeningScroll() {
        this.messageListAdapter?.startListeningScroll()
    }

    open fun stopListeningScroll() {
        this.messageListAdapter?.stopListeningScroll()
    }

    open fun deleteMessages(messages: List<ChatMessage>) {
        this.messageListAdapter?.deleteByIds(messages.map { it.id }.toTypedArray())
    }

    fun getMessageStyle(): MessageStyle {
        return MessageStyle(this.avatarHiddenForOutgoingMessages, this.avatarHiddenForIncomingMessages, this.messageSenderTextColor)
    }

    fun MessagesFromChatMessages(chatMessages: List<ChatMessage>): List<Message> {
        val multitypeMessages = chatMessages.map {
            MessageFactory.getMessage(it, getMessageStyle())
        }
        multitypeMessages.forEach { updateMessageAuthor(it) }
        return multitypeMessages
    }

    fun MessageFromChatMessage(chatMessage: ChatMessage, uri: Uri?): Message {
        val multitypeMessage = MessageFactory.getMessage(chatMessage, getMessageStyle(), uri)
        updateMessageAuthor(multitypeMessage)
        return multitypeMessage
    }

    fun updateMessageAuthor(message: Message) {
        val ownerId = message.chatMessage.record.ownerId
        message.author = when {
            ownerId == null || ownerId == "" -> userBuilder.createUser(this.skygear?.auth?.currentUser!!)
            userMap.containsKey(ownerId) -> userMap[ownerId]
            else -> userBuilder.createUser(ownerId)
        }
    }

    fun updateAuthors(authors: List<Record>) {
        authors.forEach {
            userMap[it.ownerId] = userBuilder.createUser(it)
        }
        this.messageListAdapter?.updateMessagesAuthor(userMap)
    }

    open fun getOtherParticipantsTitle(callback: ((String?) -> Unit)? ) {
        val currentUserId = this.skygear?.auth?.currentUser?.id
        val otherParticipantIds = conversation?.participantIds?.filter { p -> p != currentUserId }
        otherParticipantIds?.let { it ->
            this.userCache?.getUsers(it, { userIDs ->
                val names = otherParticipantIds?.map {
                    val key = userIDs[it]?.displayNameField
                    userIDs[it]?.chatUser?.record?.get(key) ?: userIDs[it]?.chatUser?.record?.get(User.DefaultUsernameField)
                }
                val newTitle = names?.joinToString(", ")
                callback?.invoke(newTitle)
            })
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
