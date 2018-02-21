package io.skygear.plugins.chat.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.skygear.chatkit.messages.VoiceMessageOnClickListener
import io.skygear.chatkit.messages.MessagesListAdapter
import io.skygear.plugins.chat.* // ktlint-disable no-wildcard-imports
import io.skygear.plugins.chat.ui.model.* // ktlint-disable no-wildcard-imports
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.utils.* // ktlint-disable no-wildcard-imports
import io.skygear.skygear.* // ktlint-disable no-wildcard-imports
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.* // ktlint-disable no-wildcard-imports
import io.skygear.plugins.chat.Conversation as ChatConversation
import io.skygear.plugins.chat.Message as ChatMessage

open class ConversationFragment() :
        Fragment(),
        MessagesListAdapter.OnLoadMoreListener,
        MessagesListAdapter.OnMessageClickListener<Message>,
        MessagesListAdapter.OnMessageLongClickListener<Message>,
        VoiceMessagePlayer.OnMessageStateChangeListener,
        VoiceMessagePlayer.OnPlayerErrorListener,
        VoiceMessageOnClickListener {
    companion object {
        val ConversationBundleKey = "CONVERSATION"
        val LayoutResIdBundleKey = "LAYOUT"
        val AvatarAdapterBundleKey = "AVATAR_ADAPTER"
        val TitleOptionBundleKey = "TITLE_OPTION"
        val ConversationViewAdapterBundleKey = "VIEW_ADAPTER"
        val MessageSentListenerKey = "MESSAGE_SENT_LISTENER"
        val MessageFetchListenerKey = "MESSAGE_FETCH_LISTENER"
        val ConnectionListenerKey = "CONNECTION_LISTENER"
        private val TAG = "ConversationFragment"
        private val MESSAGE_SUBSCRIPTION_MAX_RETRY = 10
        private val REQUEST_PICK_IMAGES = 5001
        private val REQUEST_IMAGE_CAPTURE = 5002
        private val REQUEST_CAMERA_PERMISSION = 5003
        private val REQUEST_VOICE_RECORDING_PERMISSION = 5004
        private val MIN_VOICE_DURATION_MS = 1000
    }

    var conversation: ChatConversation? = null

    private var skygear: Container? = null
    private var skygearChat: ChatContainer? = null

    private var messageIDs: HashSet<String> = HashSet()
    private var messageErrorByIDs: HashMap<String, Error> = hashMapOf()

    private var voiceRecorder: MediaRecorder? = null
    private var voiceRecordingFileName: String? = null
    private var voicePlayer: VoiceMessagePlayer? = null

    private var messageLoadMoreBefore: ChatMessage? = null
    private var messageSubscriptionRetryCount = 0

    private var mCameraPhotoUri: Uri? = null

    private var takePhotoPermissionManager: PermissionManager? = null
    private var voiceRecordingPermissionManager: PermissionManager? = null

    protected var layoutResID: Int = -1
    protected var customAvatarAdapter: AvatarAdapter? = null
    protected var customViewAdapter: ConversationViewAdapter? = null
    protected var fragmentMessageSentListener: MessageSentListener? = null
    protected var fragmentMessageFetchListener: MessageFetchListener? = null
    protected var titleOption: ConversationTitleOption? = ConversationTitleOption.DEFAULT
    protected var connectionListener: ConnectionListener? = null
    protected var pubsubListener: PubsubListener? = null

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutResID = arguments.getInt(LayoutResIdBundleKey, R.layout.conversation_fragment)
        arguments.getString(ConversationBundleKey)?.let { json ->
            conversation = ChatConversation.fromJson(JSONObject(json))
        }
        arguments.getSerializable(AvatarAdapterBundleKey)?.let { adapter ->
            customAvatarAdapter = adapter as AvatarAdapter
        }
        arguments.getSerializable(TitleOptionBundleKey)?.let { option ->
            titleOption = option as ConversationTitleOption
        }
        arguments.getSerializable(ConversationViewAdapterBundleKey)?.let { adapter ->
            customViewAdapter = adapter as ConversationViewAdapter
        }
        arguments.getSerializable(MessageSentListenerKey)?.let { listener ->
            fragmentMessageSentListener = listener as MessageSentListener
        }
        arguments.getSerializable(MessageFetchListenerKey)?.let { listener ->
            fragmentMessageFetchListener = listener as MessageFetchListener
        }
        arguments.getSerializable(ConnectionListenerKey)?.let { listener ->
            connectionListener = listener as ConnectionListener
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        this.skygear = Container.defaultContainer(context)
        this.skygearChat = ChatContainer.getInstance(this.skygear as Container)
        this.voicePlayer = VoiceMessagePlayer(this.activity)
        this.voicePlayer?.playerErrorListener = this
        this.voicePlayer?.messageStateChangeListener = this
    }

    private fun createPhotoPermissionManager(activity: Activity): PermissionManager {
        return object : PermissionManager(
                activity,
                listOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                permissionGrantedHandler = { this.takePhotoFromCameraIntent() },
                permissionDeniedHandler = { permissionsDenied, _ ->
                    this@ConversationFragment.takePhotoPermissionsDenied(permissionsDenied)
                }
        ) {
            override fun request(permissions: List<String>) {
                this@ConversationFragment.requestPermissions(
                        permissions.toTypedArray(),
                        ConversationFragment.REQUEST_CAMERA_PERMISSION
                )
            }
        }
    }

    private fun createRecordingPermissionManager(activity: Activity): PermissionManager {
        return object : PermissionManager(
                activity,
                listOf(Manifest.permission.RECORD_AUDIO),
                permissionDeniedHandler = { _, _ ->
                    this@ConversationFragment.voiceRecordingPermissionDenied()
                }
        ) {
            override fun request(permissions: List<String>) {
                this@ConversationFragment.requestPermissions(
                        permissions.toTypedArray(),
                        ConversationFragment.REQUEST_VOICE_RECORDING_PERMISSION
                )
            }
        }
    }

    protected fun createConversationView(inflater: LayoutInflater?, container: ViewGroup?): ConversationView {
        val view = inflater?.inflate(layoutResID, container, false) as ConversationView

        view.setAddAttachmentButtonOnClickListener { _ -> this@ConversationFragment.onAddAttachmentButtonClick() }

        view.setVoiceButtonHolderListener(object : HoldingButtonLayoutBaseListener() {
            override fun onExpand() {
                super.onExpand()
                this@ConversationFragment.onVoiceRecordingButtonPressedDown()
            }

            override fun onCollapse(isCancel: Boolean) {
                super.onCollapse(isCancel)
                this@ConversationFragment.onVoiceRecordingButtonPressedUp(isCancel)
            }
        })

        view.setSendTextMessageListener { msg -> this@ConversationFragment.onSendMessage(msg) }
        view.setOnMessageClickListener(this)
        view.setOnMessageLongClickListener(this)
        view.setVoiceMessageOnClickListener(this)
        view.setLoadMoreListener(this)
        view.setConversation(conversation)
        customAvatarAdapter?.let {
            adapter -> view.setAvatarAdapter(adapter)
        }

        customViewAdapter?.let {
            adapter -> view.setViewAdapter(adapter)
        }
        return view
    }

    fun conversationView(): ConversationView? {
        return this.view as ConversationView?
    }

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val view = createConversationView(inflater, container)
        if (titleOption == ConversationTitleOption.DEFAULT) {
            this.activity.title = this.conversation?.title
        }
        // TODO: setup typing indicator subscription

        this.takePhotoPermissionManager = createPhotoPermissionManager(this.activity)
        this.voiceRecordingPermissionManager = createRecordingPermissionManager(this.activity)

        return view
    }

    override fun onStop() {
        super.onStop()
        this.voicePlayer?.stop()
    }

    override fun onResume() {
        super.onResume()
        if (conversationView()?.itemCount() == 0) {
            this.conversation?.let {
                this.fetchUnsentMessages()
                this.fetchMessages()
            }
        }
        this.connectionListener?.let {
            this.pubsubListener = object : PubsubListener {
                override fun onClose() {
                    connectionListener?.onClose(this@ConversationFragment)
                }

                override fun onOpen() {
                    connectionListener?.onOpen(this@ConversationFragment)
                }

                override fun onError(e: Exception?) {
                    connectionListener?.onError(this@ConversationFragment, e)
                }
            }
            this.skygearChat?.setPubsubListener(pubsubListener)
        }
        this.fetchParticipants()

        this.messageSubscriptionRetryCount = 0
        this.subscribeMessage()
    }

    override fun onPause() {
        super.onPause()
        this.pubsubListener = null
        this.skygearChat?.setPubsubListener(null)
        this.unsubscribeMessage()
    }

    private fun fetchParticipants() {
        this.conversation?.let { conv ->
            val userIDs = conv.participantIds.orEmpty()
            if (userIDs.isEmpty()) {
                return
            }

            val q = Query("user").contains("_id", userIDs.toList())
            this.skygear?.publicDatabase?.query(q, object : RecordQueryResponseHandler() {
                override fun onQueryError(error: Error?) {
                }

                override fun onQuerySuccess(records: Array<out Record>?) {
                    records?.let {
                        conversationView()?.updateAuthors(records.toList())
                        if (titleOption == ConversationTitleOption.OTHER_PARTICIPANTS) {
                            this@ConversationFragment.activity.title = conversationView()?.getOtherParticipantsTitle()
                        }
                    }
                }
            })
        }
    }

    private fun fetchMessages(
            before: ChatMessage? = null,
            complete: ((msgs: List<ChatMessage>?, error: String?) -> Unit)? = null
    ) {

        val cachedResult: MutableList<ChatMessage> = mutableListOf()

        this.conversationView()?.stopListeningScroll()

        val successCallback = fun(chatMsgs: List<ChatMessage>?, isCached: Boolean) {
            callMessageFetchSuccessListener(chatMsgs, isCached)
            conversationView()?.hideProgress()
            if (isCached) {
                chatMsgs?.let { cachedResult.addAll(it) }
            } else {
                chatMsgs?.let { fetchedMessages ->
                    if (fetchedMessages.isNotEmpty()) {
                        this.conversationView()?.startListeningScroll()
                    }

                    // remove cached message if not found in fetched results
                    val cachedResultToRemove = cachedResult.filter { message ->
                        fetchedMessages.none { it.id.equals(message.id) } && chatMsgs.any { it.id.equals(message.id) }
                    }

                    this@ConversationFragment.deleteMessages(cachedResultToRemove)
                }
            }

            chatMsgs?.let {
                this@ConversationFragment.addMessages(it)
                val minMessage = it.minBy { m -> m.sequence }
                minMessage?.let {
                    val oldBefore = this@ConversationFragment.messageLoadMoreBefore
                    if (oldBefore == null || minMessage.sequence < oldBefore.sequence) {
                        messageLoadMoreBefore = minMessage
                    }
                }
            }

            complete?.let { it(chatMsgs, null) }
        }

        this.fragmentMessageFetchListener?.onBeforeMessageFetch(this)
        this.conversation?.let { conv ->
            this.skygearChat?.getMessages(
                    conv,
                    0,
                    before,
                    null,
                    object : GetMessagesCallback {
                        override fun onSuccess(chatMsgs: List<ChatMessage>?) {
                            successCallback(chatMsgs, false)
                        }

                        override fun onGetCachedResult(chatMsgs: List<ChatMessage>?) {
                            successCallback(chatMsgs, true)
                        }

                        override fun onFail(error: Error) {
                            Log.w(TAG, "Failed to get message: %s".format(error.message))
                            this@ConversationFragment.fragmentMessageFetchListener?.onMessageFetchFailed(this@ConversationFragment, error)
                            complete?.let { it(null, error.message) }
                        }
                    })
        }
    }

    private fun fetchUnsentMessages() {
        this.conversation?.let { conv ->
            this.skygearChat?.fetchOutstandingMessageOperations(conv,
                    MessageOperation.Type.ADD, object : GetCallback<List<MessageOperation>> {
                override fun onSuccess(operations: List<MessageOperation>?) {
                    if (operations != null) {
                        for (operation: MessageOperation in operations) {
                            if (operation.status != MessageOperation.Status.FAILED) {
                                continue
                            }

                            var error: Error = when (operation.error) {
                                null -> Error("Error occurred sending message.")
                                else -> operation.error
                            }
                            this@ConversationFragment.addMessages(listOf(operation.message), error)
                        }
                    }
                }

                override fun onFail(error: Error) {
                    Log.w(TAG, "Failed to get unsent message: %s".format(error.message))
                }
            })
        }
    }

    /**
     * This function is for receiving new message and sending new message.
     */
    private fun addMessageToBottom(message: ChatMessage, uri: Uri? = null) {
        val view = conversationView()
        if (messageIDs.contains(message.id)) {
            view?.updateMessages(listOf(message))
        } else {
            view?.addMessageToBottom(message, uri)
            messageIDs.add(message.id)
        }

        // mark last read message
        this.conversation?.let { conv ->
            this.skygearChat?.markConversationLastReadMessage(conv, message)
        }
        this.skygearChat?.markMessageAsRead(message)
    }

    private fun addMessages(messages: List<ChatMessage>) {
        addMessages(messages, null)
    }

    /**
     * This function is for loading more previous messages.
     */
    private fun addMessages(messages: List<ChatMessage>, error: Error?) {
        if (messages.isEmpty()) {
            return
        }

        val view = conversationView()
        val messagesAddToEnd = mutableListOf<ChatMessage>()
        val messagesToUpdate = mutableListOf<ChatMessage>()
        messages.forEach { msg ->
            when {
                messageIDs.contains(msg.id) -> messagesToUpdate.add(msg)
                else -> messagesAddToEnd.add(msg)
            }

            messageIDs.add(msg.id)
            if (error is Error) {
                messageErrorByIDs[msg.id] = error
            } else {
                messageErrorByIDs.remove(msg.id)
            }
        }

        if (messagesToUpdate.isNotEmpty()) {
            if (error is Error) {
                view?.updateMessages(messagesToUpdate, error)
            } else {
                view?.updateMessages(messagesToUpdate)
            }
        }

        if (messagesAddToEnd.isNotEmpty()) {
            if (error is Error) {
                view?.mergeMessages(messagesAddToEnd, error)
            } else {
                view?.mergeMessages(messagesAddToEnd)
            }
        }

        // mark last read message
        this.conversation?.let { conv ->
            val lastChatMsg = messages.last()
            this.skygearChat?.markConversationLastReadMessage(conv, lastChatMsg)
        }

        this.skygearChat?.markMessagesAsRead(messages)
    }

    private fun updateMessage(message: ChatMessage) {
        messageIDs.add(message.id)
        messageErrorByIDs.remove(message.id)

        conversationView()?.updateMessages(listOf(message))
    }

    private fun updateMessage(message: ChatMessage, error: Error) {
        if (messageErrorByIDs.containsKey(message.id)) {
            return
        }
        messageIDs.add(message.id)
        messageErrorByIDs[message.id] = error

        conversationView()?.updateMessages(listOf(message), error)
    }

    private fun deleteMessages(messages: List<ChatMessage>) {
        if (messages.isEmpty()) {
            return
        }

        val view = conversationView()
        view?.deleteMessages(messages)
        messages?.map {
            this.messageIDs.remove(it.id)
            this.messageErrorByIDs.remove(it.id)
        }
    }

    private fun subscribeMessage() {
        if (this.messageSubscriptionRetryCount >= MESSAGE_SUBSCRIPTION_MAX_RETRY) {
            Log.i(TAG, "Message subscription retry has reach the maximum, abort.")
            return
        }
        this.messageSubscriptionRetryCount++
        this.conversation?.let { conv ->
            this.skygearChat?.subscribeConversationMessage(
                    conv,
                    object : MessageSubscriptionCallback(conv) {
                        override fun notify(eventType: String,
                                            message: ChatMessage
                        ) {
                            when (eventType) {
                                EVENT_TYPE_CREATE ->
                                    this@ConversationFragment.onReceiveChatMessage(message)
                                EVENT_TYPE_UPDATE ->
                                    this@ConversationFragment.onUpdateChatMessage(message)
                            }
                        }

                        override fun onSubscriptionFail(error: Error) {
                            this@ConversationFragment.subscribeMessage()
                        }
                    })
        }
    }

    private fun unsubscribeMessage() {
        this.conversation?.let { conv ->
            this.skygearChat?.unsubscribeConversationMessage(conv)
        }
    }

    private fun onReceiveChatMessage(msg: ChatMessage) {
        this.addMessageToBottom(msg)
    }

    private fun onUpdateChatMessage(message: ChatMessage) {
        this.updateMessage(message)
    }

    override fun onLoadMore(totalItemsCount: Int) {
        this.fetchMessages(before = this.messageLoadMoreBefore)
    }

    override fun onVoiceMessageClick(voiceMessage: VoiceMessage) {
        if (this.voicePlayer?.message == voiceMessage) {
            when (voiceMessage.state) {
                VoiceMessage.State.INITIAL, VoiceMessage.State.PAUSED -> this.voicePlayer?.play()
                VoiceMessage.State.PLAYING -> this.voicePlayer?.pause()
            }
        } else {
            this.voicePlayer?.stop()
            this.voicePlayer?.message = voiceMessage
            this.voicePlayer?.play()
        }
    }

    override fun onVoiceMessageStateChanged(voiceMessage: VoiceMessage) {
        Log.i(TAG, "Voice Message State Changed: ${voiceMessage.state}")
        conversationView()?.updateVoiceMessage(voiceMessage)
    }

    override fun onVoiceMessagePlayerError(error: VoiceMessagePlayer.Error) {
        Toast.makeText(activity, error.message, Toast.LENGTH_SHORT).show()
    }

    fun onAddAttachmentButtonClick() {
        AlertDialog.Builder(this.context)
                .setItems(R.array.attachment_options) { _, option ->
                    when (option) {
                        0 -> this@ConversationFragment.takePhotoFromCameraIntent()
                        1 -> {
                            val intent = Intent()

                            intent.type = "image/*"
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            intent.action = Intent.ACTION_GET_CONTENT

                            this@ConversationFragment.startActivityForResult(
                                    Intent.createChooser(intent, "Select Photos"),
                                    REQUEST_PICK_IMAGES
                            )
                        }
                    }
                }
                .show()
    }

    fun onVoiceRecordingButtonPressedDown() {
        conversationView()?.toggleVoiceButtonHint(true)

        val fileDir = this.context.cacheDir.absolutePath
        val fileName = "voice-${Date().time}.${VoiceMessage.FILE_EXTENSION_NAME}"
        this.voiceRecordingFileName = "$fileDir/$fileName"

        this.voiceRecordingPermissionManager?.runIfPermissionGranted {
            this.voiceRecorder = MediaRecorder()
            this.voiceRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            this.voiceRecorder?.setOutputFormat(VoiceMessage.MEDIA_FORMAT)
            this.voiceRecorder?.setOutputFile(voiceRecordingFileName)
            this.voiceRecorder?.setAudioEncoder(VoiceMessage.MEDIA_ENCODING)

            this.voiceRecorder?.prepare()
            this.voiceRecorder?.start()
        }

        conversationView()?.startVoiceRecordingTimer()
    }

    fun onVoiceRecordingButtonPressedUp(isCancel: Boolean) {
        conversationView()?.toggleVoiceButtonHint(false)
        conversationView()?.stopVoiceRecordingTimer()
        if (this.voiceRecordingPermissionManager?.permissionsGranted() != true) {
            return
        }

        // finish recording
        try {
            this.voiceRecorder?.stop()
            this.voiceRecorder?.release()
        } catch (e: RuntimeException) {
            Log.w(ConversationFragment.TAG, "Some errors occurs in voice recorder: $e")
        }
        this.voiceRecorder = null

        if (isCancel) {
            File(this.voiceRecordingFileName!!).delete()
            this.voiceRecordingFileName = null
            return
        }

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(this.voiceRecordingFileName!!)

        val duration = Integer.parseInt(
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))

        if (duration < MIN_VOICE_DURATION_MS) {
            Toast.makeText(
                    activity,
                    getString(R.string.voice_too_short),
                    Toast.LENGTH_SHORT).show()
            return
        }

        val voiceRecordingFile = File(this.voiceRecordingFileName!!)
        val bytes = ByteArray(voiceRecordingFile.length().toInt())
        val stream = BufferedInputStream(FileInputStream(voiceRecordingFile))
        stream.read(bytes, 0, bytes.size)
        stream.close()

        this.conversation?.let { conv ->
            val fileName = voiceRecordingFileName!!.split("/").last()
            val asset = Asset(fileName, VoiceMessage.MIME_TYPE, bytes)

            val meta = JSONObject()
            meta.put(VoiceMessage.DurationMatadataName, duration)

            val message = ChatMessage()
            message.asset = asset
            message.metadata = meta

            this.fragmentMessageSentListener?.onBeforeMessageSent(this, message)
            addMessageToBottom(message, Uri.parse("file://" + voiceRecordingFileName))
            this.skygearChat?.addMessage(message, conv, object : SaveCallback<ChatMessage> {
                override fun onSuccess(chatMsg: ChatMessage?) {
                    voiceRecordingFile.delete()
                    callMessageSentSuccessListener(message)
                }

                override fun onFail(error: Error) {
                    Log.e(
                            ConversationFragment.TAG,
                            "Failed to send voice message: ${error.message}"
                    )
                    this@ConversationFragment.fragmentMessageSentListener?.onMessageSentFailed(this@ConversationFragment, message, error)
                }
            })
        }
    }

    fun callMessageSentSuccessListener(chatMsg: ChatMessage?) {
        chatMsg?.let {
            this@ConversationFragment.fragmentMessageSentListener
                    ?.onMessageSentSuccess(this@ConversationFragment, chatMsg)
        } ?: run {
            this@ConversationFragment.fragmentMessageSentListener
                    ?.onMessageSentFailed(this@ConversationFragment,
                            null, Error("No message sent"))
        }
    }

    fun callMessageFetchSuccessListener(chatMsgs: List<ChatMessage> ?, isCached: Boolean) {
        chatMsgs?.let {
            this@ConversationFragment.fragmentMessageFetchListener?.onMessageFetchSuccess(this@ConversationFragment, it, isCached)
        } ?: run {
            this@ConversationFragment.fragmentMessageFetchListener
                    ?.onMessageFetchFailed(this@ConversationFragment,
                            Error("No message fetched"))
        }
    }

    fun onSendMessage(input: String): Boolean {

        this.conversation?.let { conv ->
            val message = ChatMessage()
            message.body = input.trim()
            this.fragmentMessageSentListener?.onBeforeMessageSent(this, message)
            this.addMessageToBottom(message)
            this.skygearChat?.addMessage(message, conv, object : SaveCallback<ChatMessage> {
                override fun onSuccess(msg: io.skygear.plugins.chat.Message?) {
                    callMessageSentSuccessListener(message)
                    msg?.let { this@ConversationFragment.updateMessage(msg) }
                }

                override fun onFail(error: Error) {
                    this@ConversationFragment.fragmentMessageSentListener
                            ?.onMessageSentFailed(this@ConversationFragment, message, error)
                    message?.let { this@ConversationFragment.updateMessage(message, error) }
                }
            })
        }

        return true
    }

    override fun onMessageClick(message: Message?) {
        message?.let { msg ->
            when (msg) {
                is ImageMessage -> {
                    msg.imageUrl
                            ?.let { url -> ImagePreviewActivity.newIntent(activity, url) }
                            ?.let { intent -> this@ConversationFragment.startActivity(intent) }
                }
                is VoiceMessage -> this@ConversationFragment.onVoiceMessageClick(msg)
                else -> {
                    // Do nothing
                }
            }
        }
    }

    override fun onMessageLongClick(message: Message?) {
        message?.let { msg ->
            when (this.messageErrorByIDs[msg.chatMessage.id] != null) {
                true -> {
                    showFailedMessageDialog(msg.chatMessage)
                }
                else -> {
                    // Do nothing
                }
            }
        }
    }

    fun showFailedMessageDialog(message: ChatMessage) {
        AlertDialog.Builder(this.context)
                .setTitle("Action")
                .setPositiveButton("Resend", { dialogInterface, i ->
                    this.resendMessage(message)
                })
                .setNegativeButton("Delete", { dialogInterface, i ->
                    this.cancelMessage(message)
                })
                .show()
    }

    fun resendMessage(message: ChatMessage) {
        val messageToResend = ChatMessage(message.record)

        this.skygearChat?.fetchOutstandingMessageOperations(message, MessageOperation.Type.ADD, object : GetCallback<List<MessageOperation>> {
            override fun onSuccess(operations: List<MessageOperation>?) {
                var firstOperation = operations?.firstOrNull()
                if (firstOperation is MessageOperation) {
                    this@ConversationFragment.skygearChat?.retryMessageOperation(firstOperation, object : MessageOperationCallback {
                        override fun onSuccess(operation: MessageOperation, message: io.skygear.plugins.chat.Message) {
                            message.let { this@ConversationFragment.updateMessage(message) }
                        }

                        override fun onFail(error: Error) {
                            this@ConversationFragment.updateMessage(messageToResend, error)
                        }
                    })
                }
            }

            override fun onFail(error: Error) {
                Log.w(TAG, "Failed to get message operation: %s".format(error.message))
            }
        })

        this.deleteMessages(listOf(message))
        this.addMessageToBottom(messageToResend)
    }

    fun cancelMessage(message: ChatMessage) {
        this.skygearChat?.fetchOutstandingMessageOperations(message, MessageOperation.Type.ADD, object : GetCallback<List<MessageOperation>> {
            override fun onSuccess(operations: List<MessageOperation>?) {
                var firstOperation = operations?.firstOrNull()
                if (firstOperation is MessageOperation) {
                    this@ConversationFragment.skygearChat?.cancelMessageOperation(firstOperation)
                }
            }

            override fun onFail(error: Error) {
                Log.w(TAG, "Failed to get message operation: %s".format(error.message))
            }
        })

        this.deleteMessages(listOf(message))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMAGES && resultCode == Activity.RESULT_OK) {
            val clipData = data?.clipData
            if (clipData == null) {
                // selected one image
                data?.data?.let { this@ConversationFragment.sendImageMessage(it) }
            } else {
                // selected multiple images
                IntRange(0, clipData.itemCount - 1)
                        .map { idx -> clipData.getItemAt(idx).uri }
                        .forEach { uri -> this@ConversationFragment.sendImageMessage(uri) }
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            mCameraPhotoUri?.let { this@ConversationFragment.sendImageMessage(it) }
            mCameraPhotoUri = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        (when (requestCode) {
            ConversationFragment.REQUEST_VOICE_RECORDING_PERMISSION ->
                this@ConversationFragment.voiceRecordingPermissionManager
            ConversationFragment.REQUEST_CAMERA_PERMISSION ->
                this@ConversationFragment.takePhotoPermissionManager
            else -> null
        })?.notifyRequestResult(permissions.toList(), grantResults.toList())
    }

    fun sendImageMessage(imageUri: Uri) {
        val imageData = getResizedBitmap(context, imageUri)
        if (imageData == null) {
            Log.w(TAG, "Failed to decode image from uri: %s".format(imageUri))
            return
        }

        this.conversation?.let { conv ->
            val imageMessage = MessageBuilder.createImageMessage(imageData)
            this.fragmentMessageSentListener?.onBeforeMessageSent(this, imageMessage)
            this.addMessageToBottom(imageMessage, imageUri)
            this.skygearChat?.addMessage(imageMessage, conv, object : SaveCallback<io.skygear.plugins.chat.Message> {
                override fun onSuccess(message: ChatMessage?) {
                    callMessageSentSuccessListener(message)
                }

                override fun onFail(error: Error) {
                    this@ConversationFragment.fragmentMessageSentListener
                            ?.onMessageSentFailed(this@ConversationFragment, imageMessage, error)
                }
            })
        }
    }

    private fun takePhotoFromCameraIntent() {
        this.takePhotoPermissionManager?.runIfPermissionGranted {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(activity.packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile(activity)
                } catch (e: IOException) {
                    // Error occurred while creating the File
                }
                if (photoFile != null) {
                    mCameraPhotoUri = FileProvider.getUriForFile(activity,
                            activity.packageName + ".fileprovider",
                            photoFile)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraPhotoUri)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            } else {
                Toast.makeText(
                        activity,
                        getString(R.string.camera_is_not_available),
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun takePhotoPermissionsDenied(permissionsDenied: List<String>) {
        when {
            permissionsDenied.size > 1 ->
                R.string.please_turn_on_camera_and_write_external_storage_permissions
            permissionsDenied.contains(Manifest.permission.CAMERA) ->
                R.string.please_turn_on_camera_permission
            permissionsDenied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE) ->
                R.string.please_turn_on_write_external_storage_permissions
            else -> null
        }?.let { msgId ->
            Toast.makeText(activity, msgId, Toast.LENGTH_SHORT).show()
        }
    }

    private fun voiceRecordingPermissionDenied() {
        conversationView()?.cancelVoiceButton()
        conversationView()?.stopVoiceRecordingTimer()
        Toast.makeText(
                activity,
                R.string.please_turn_on_audio_recording_permission,
                Toast.LENGTH_SHORT
        ).show()
    }

    open fun setAvatarAdapter(newAdapter: AvatarAdapter?) {
        customAvatarAdapter = newAdapter
    }

    open fun setViewAdapter(newAdapter: ConversationViewAdapter?) {
        customViewAdapter = newAdapter
    }
}
