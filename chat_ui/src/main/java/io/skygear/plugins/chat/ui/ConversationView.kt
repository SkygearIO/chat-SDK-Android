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
import io.skygear.plugins.chat.ui.utils.ImageLoader
import android.app.Activity
import android.os.Handler
import android.widget.* // ktlint-disable no-wildcard-imports
import io.skygear.plugins.chat.ui.holder.* // ktlint-disable no-wildcard-imports
import io.skygear.plugins.chat.ui.model.* // ktlint-disable no-wildcard-imports
import io.skygear.plugins.chat.ui.utils.AvatarBuilder
import io.skygear.plugins.chat.Message as ChatMessage
import io.skygear.skygear.Container
import io.skygear.chatkit.messages.CustomMessageHolders
import io.skygear.plugins.chat.Conversation
import io.skygear.chatkit.messages.VoiceMessageOnClickListener
import io.skygear.chatkit.messages.MessageHolders
import io.skygear.chatkit.messages.MessagesList
import io.skygear.chatkit.messages.MessagesListAdapter
import io.skygear.plugins.chat.Participant
import io.skygear.skygear.Error

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

open class ConversationView : RelativeLayout {
    private var messagesListView: MessagesList? = null
    private var messageErrorByIDs: MutableMap<String, Error> = mutableMapOf()
    private var addAttachmentButton: ImageButton? = null
    private var messageSendButton: ImageButton? = null
    private var messageEditText: EditText? = null
    private var voiceButtonHolderHint: View? = null
    private var voiceRecordingSeconds: TextView? = null
    private var voiceButtonHolder: HoldingButtonLayout? = null
    private var progressBar: ProgressBar? = null
    private var messagesListViewReachBottomListener: MessagesListViewReachBottomListener? = null
    private var sendTextMessageListener: (String) -> Boolean? = { true }
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
    private var delivering: String? = null
    private var delivered: String? = null
    private var someRead: String? = null
    private var allRead: String? = null
    private var failedText: String? = null
    private var hint: String? = null
    private var dateFormat: String

    private var timeTextColorForIncomingMessages: Int
    private var timeTextColorForOutgoingMessages: Int

    private var backgroundColorForIncomingMessages: Int
    private var backgroundColorForOutgoingMessages: Int

    private var voiceMessageButtonColorForIncomingMessages: Int
    private var voiceMessageButtonColorForOutgoingMessages: Int

    private var viewAdapter: ConversationViewAdapter? = null
    private var backgroundImageView: ImageView

    private var textColorForIncomingMessages: Int
    private var textColorForOutgoingMessages: Int

    private var statusTextColorForIncomingMessages: Int
    private var statusTextColorForOutgoingMessages: Int

    private var voiceMessageButtonShouldShow: Boolean
    private var cameraButtonShouldShow: Boolean
    private var messageStatusShouldShow: Boolean

    private val mHandlerTime = Handler()
    private var voiceDuration = 0

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        val a = context.theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.ConversationView,
                0, 0)

        try {
            avatarNameField = a.getString(R.styleable.ConversationView_userNameField) ?: User.DefaultUsernameField
            avatarImageField = a.getString(R.styleable.ConversationView_userAvatarField) ?: User.DefaultAvatarField
            avatarHiddenForOutgoingMessages = a.getBoolean(R.styleable.ConversationView_avatarHiddenForOutgoingMessages, true)
            avatarHiddenForIncomingMessages = a.getBoolean(R.styleable.ConversationView_avatarHiddenForIncomingMessages, false)
            messageSenderTextColor = a.getColor(R.styleable.ConversationView_messageSenderTextColor, Color.BLACK)
            userAvatarType = AvatarType.fromInt(a.getInt(R.styleable.ConversationView_userAvatarType, AvatarType.INITIAL.value))
            avatarTextColor = a.getColor(R.styleable.ConversationView_avatarTextColor, Color.WHITE)
            avatarBackgroundColor = a.getColor(R.styleable.ConversationView_avatarBackgroundColor, ContextCompat.getColor(context, R.color.blue_1))

            delivering = a.getString(R.styleable.ConversationView_delivering)
            delivered = a.getString(R.styleable.ConversationView_delivered)
            someRead = a.getString(R.styleable.ConversationView_someRead)
            allRead = a.getString(R.styleable.ConversationView_allRead)
            failedText = a.getString(R.styleable.ConversationView_failed)
            hint = a.getString(R.styleable.ConversationView_hint)
            dateFormat = a.getString(R.styleable.ConversationView_dateFormat) ?: "HH:mm"
            val grayDarkColor = ContextCompat.getColor(context, R.color.gray_dark)
            timeTextColorForIncomingMessages = a.getColor(R.styleable.ConversationView_timeTextColorForIncomingMessages, grayDarkColor)
            timeTextColorForOutgoingMessages = a.getColor(R.styleable.ConversationView_timeTextColorForOutgoingMessages, grayDarkColor)
            backgroundColorForIncomingMessages = a.getColor(R.styleable.ConversationView_backgroundColorForIncomingMessages, ContextCompat.getColor(context, R.color.white_two))
            backgroundColorForOutgoingMessages = a.getColor(R.styleable.ConversationView_backgroundColorForOutgoingMessages, ContextCompat.getColor(context, R.color.cornflower_blue_two))
            voiceMessageButtonColorForIncomingMessages = a.getColor(R.styleable.ConversationView_voiceMessageButtonColorForIncomingMessages, ContextCompat.getColor(context, R.color.white))
            voiceMessageButtonColorForOutgoingMessages = a.getColor(R.styleable.ConversationView_voiceMessageButtonColorForOutgoingMessages, ContextCompat.getColor(context, R.color.white))
            textColorForIncomingMessages = a.getColor(R.styleable.ConversationView_textColorForIncomingMessages, ContextCompat.getColor(context, R.color.black))
            textColorForOutgoingMessages = a.getColor(R.styleable.ConversationView_textColorForOutgoingMessages, ContextCompat.getColor(context, R.color.white))
            statusTextColorForIncomingMessages = a.getColor(R.styleable.ConversationView_statusTextColorForIncomingMessages, grayDarkColor)
            statusTextColorForOutgoingMessages = a.getColor(R.styleable.ConversationView_statusTextColorForOutgoingMessages, grayDarkColor)
            voiceMessageButtonShouldShow = a.getBoolean(R.styleable.ConversationView_voiceMessageButtonShouldShow, true)
            cameraButtonShouldShow = a.getBoolean(R.styleable.ConversationView_cameraButtonShouldShow, true)
            messageStatusShouldShow = a.getBoolean(R.styleable.ConversationView_messageStatusShouldShow, true)
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
        this.backgroundImageView = findViewById<ImageView>(R.id.background)
        this.voiceButtonHolderHint = findViewById(R.id.voice_recording_btn_holder_hint)
        this.voiceRecordingSeconds = findViewById<TextView>(R.id.voice_recording_seconds)
        this.voiceButtonHolder = findViewById<HoldingButtonLayout>(R.id.voice_recording_btn_holder)

        this.progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val conversationView = this

        toggleVoiceButton(voiceMessageButtonShouldShow)
        toggleCameraButton(cameraButtonShouldShow)

        hint?.let { this.messageEditText?.hint = it }
        this.messageEditText?.addTextChangedListener(object : TextBaseWatcher() {
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                conversationView.messageEditText?.text?.let { msgContent ->
                    if (voiceMessageButtonShouldShow) {
                        if (msgContent.isEmpty()) {
                            toggleVoiceButton(true)
                        } else {
                            toggleVoiceButton(false)
                        }
                    }
                }
            }
        })

        this.messageSendButton?.setOnClickListener {
            _ ->
                messageEditText?.text?.let { text ->
                    if (!text.isBlank()) {
                        val result = this.sendTextMessageListener.invoke(text.trim().toString())
                        result?.let { success ->
                            if (success) {
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

    fun toggleVoiceButton(flag: Boolean) {
        if (flag) {
            voiceButtonHolder?.visibility = View.VISIBLE
            messageSendButton?.visibility = View.INVISIBLE
        } else {
            voiceButtonHolder?.visibility = View.INVISIBLE
            messageSendButton?.visibility = View.VISIBLE
        }
    }

    fun toggleCameraButton(flag: Boolean) {
        addAttachmentButton?.visibility = if (flag) View.VISIBLE else View.GONE
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
        return this.messagesListViewReachBottomListener?.isReachEnd !!
    }

    fun createMessageListAdapter(imageLoader: ImageLoader, senderId: String): SortedMessageListAdapter {
        messageHolders = CustomMessageHolders({ avatarAdapter }, { conversation })
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

    open fun setViewAdapter(newAdapter: ConversationViewAdapter) {
        this.viewAdapter = newAdapter
        this.viewAdapter?.setBackground(this, this.conversation !!, backgroundImageView)
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
        val visibility = if (flag) View.VISIBLE else View.INVISIBLE
        this.voiceButtonHolderHint?.visibility = visibility
        this.voiceRecordingSeconds?.visibility = visibility
        listOf(this.addAttachmentButton, this.messageEditText).map {
            it?.visibility = if (! flag) View.VISIBLE else View.INVISIBLE
        }
    }

    open fun cancelVoiceButton() {
        this.voiceButtonHolder?.cancel()
    }

    open fun startVoiceRecordingTimer() {
        voiceDuration = 0
        updateVoiceSeconds()
        this.mHandlerTime.postDelayed(voiceTimerRun, 1000)
    }

    open fun stopVoiceRecordingTimer() {
        this.mHandlerTime.removeCallbacks(voiceTimerRun)
    }

    private val voiceTimerRun = object : Runnable {
        override fun run() {
            voiceDuration++
            updateVoiceSeconds()
            mHandlerTime.postDelayed(this, 1000)
        }
    }

    fun updateVoiceSeconds() {
        val text = "%02d:%02d".format(voiceDuration / 60, voiceDuration % 60)
        this.voiceRecordingSeconds?.text = text
    }

    open fun updateMessages(chatMessages: List<ChatMessage>) {
        for (message: Message in messagesFromChatMessages(chatMessages)) {
            this.messageListAdapter?.update(message)
        }
    }

    open fun updateMessages(chatMessages: List<ChatMessage>, error: Error) {
        for (message: Message in messagesFromChatMessages(chatMessages)) {
            message.error = error
            this.messageListAdapter?.update(message)
        }
    }

    open fun updateVoiceMessage(voiceMessage: VoiceMessage) {
        updateMessageAuthor(voiceMessage)
        this.messageListAdapter?.update(voiceMessage)
    }

    open fun mergeMessages(chatMessages: List<ChatMessage>) {
        this.messageListAdapter?.merge(messagesFromChatMessages(chatMessages))
    }

    open fun mergeMessages(chatMessages: List<ChatMessage>, error: Error) {
        var messages = messagesFromChatMessages(chatMessages)
        for (message: Message in messages) {
            message.error = error
        }
        this.messageListAdapter?.merge(messages)
    }

    open fun addMessageToBottom(message: Message) {
        updateMessageAuthor(message)
        this.messageListAdapter?.addToStart(message, needToScrollToBottom())
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

    fun getMessageStatusText(): MessageStatusText {
        return MessageStatusText(delivering, delivered, someRead, allRead, failedText)
    }

    fun getMessageStyle(): MessageStyle {
        val timeStyle = MessageTimeStyle(timeTextColorForIncomingMessages, timeTextColorForOutgoingMessages)
        val bubbleStyle = MessageBubbleStyle(backgroundColorForIncomingMessages, backgroundColorForOutgoingMessages, textColorForIncomingMessages, textColorForOutgoingMessages)
        val voiceMessageStyle = VoiceMessageStyle(voiceMessageButtonColorForIncomingMessages, voiceMessageButtonColorForOutgoingMessages)
        val statusStyle = MessageStatusStyle(statusTextColorForIncomingMessages, statusTextColorForOutgoingMessages, messageStatusShouldShow)
        return MessageStyle(this.avatarHiddenForOutgoingMessages, this.avatarHiddenForIncomingMessages, this.messageSenderTextColor, getMessageStatusText(), dateFormat, timeStyle, bubbleStyle, voiceMessageStyle, statusStyle)
    }

    private fun messagesFromChatMessages(chatMessages: List<ChatMessage>): List<Message> {
        val multiTypeMessages = chatMessages.map {
            MessageFactory.getMessage(it, getMessageStyle())
        }
        multiTypeMessages.forEach { updateMessageAuthor(it) }
        return multiTypeMessages
    }

    fun updateMessageAuthor(message: Message) {
        val ownerId = message.chatMessage.record.ownerId
        message.author = when {
            ownerId == null || ownerId == "" -> userBuilder.createUser(this.skygear?.auth?.currentUser!!)
            userMap.containsKey(ownerId) -> userMap[ownerId]
            else -> userBuilder.createUser(ownerId)
        }
    }

    fun updateAuthors(authors: List<Participant>?) {
        authors?.forEach {
            userMap[it.id] = userBuilder.createUser(it)
        }
        this.messageListAdapter?.updateMessagesAuthor(userMap)
    }

    open fun getOtherParticipantsTitle(): String {
        val names = userMap.values.filter {
            it.participantId != this.skygear?.auth?.currentUser?.id
        }.map {
            val key = it.displayNameField
            it.participant?.record?.get(key) ?: it.participant?.record?.get(User.DefaultUsernameField)
        }
        return names?.joinToString(", ")
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
