package io.skygear.plugins.chat.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.dewarder.holdinglibrary.HoldingButtonLayout
import com.dewarder.holdinglibrary.HoldingButtonLayoutListener
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.skygear.plugins.chat.*
import io.skygear.plugins.chat.ui.holder.CustomOutcomingImageMessageViewHolder
import io.skygear.plugins.chat.ui.holder.CustomOutcomingTextMessageViewHolder
import io.skygear.plugins.chat.ui.model.*
import io.skygear.plugins.chat.ui.model.Conversation
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.utils.*
import io.skygear.skygear.Asset
import io.skygear.skygear.Container
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import io.skygear.plugins.chat.Conversation as ChatConversation
import io.skygear.plugins.chat.Message as ChatMessage

class ConversationFragment :
        Fragment(),
        MessagesListAdapter.OnLoadMoreListener,
        MessagesListAdapter.OnMessageClickListener<Message>,
        VoiceMessagePlayer.OnMessageStateChangeListener,
        VoiceMessagePlayer.OnPlayerErrorListener
{
    companion object {
        val ConversationBundleKey = "CONVERSATION"
        private val TAG = "ConversationFragment"
        private val MESSAGE_SUBSCRIPTION_MAX_RETRY = 10
        private val REQUEST_PICK_IMAGES = 5001
        private val REQUEST_IMAGE_CAPTURE = 5002
        private val REQUEST_CAMERA_PERMISSION = 5003
        private val REQUEST_VOICE_RECORDING_PERMISSION = 5004
        private val VOICE_RECORDING_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
    }

    var conversation: Conversation? = null
    var messageContentTypeChecker: ConversationFragment.ContentTypeChecker? = null

    private var messagesListView: MessagesList? = null
    private var addAttachmentButton: ImageButton? = null
    private var messageSendButton: ImageButton? = null
    private var messageEditText: EditText? = null
    private var voiceButtonHolderHint: View? = null
    private var voiceButtonHolder: HoldingButtonLayout? = null

    private var skygear: Container? = null
    private var skygearChat: ChatContainer? = null

    private var userCache: UserCache? = null
    private var messageIDs: HashSet<String> = HashSet()

    private var voiceRecorder: MediaRecorder? = null
    private var voiceRecordingFileName: String? = null
    private var voicePlayer: VoiceMessagePlayer? = null

    private var messagesListAdapter: MessagesListAdapter<Message>? = null
    private var messagesListViewReachBottomListener: MessagesListViewReachBottomListener? = null

    private var messageLoadMoreBefore: Date = Date()
    private var messageSubscriptionRetryCount = 0

    private var mCameraPhotoUri: Uri? = null

    private var takePhotoPermissionManager: PermissionManager? = null
    private var voiceRecordingPermissionManager: PermissionManager? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        this.skygear = Container.defaultContainer(context)
        this.skygearChat = ChatContainer.getInstance(this.skygear as Container)
        this.userCache = UserCache.getInstance(
                this.skygear as Container,
                this.skygearChat as ChatContainer
        )
        this.voicePlayer = VoiceMessagePlayer(this.activity)
        this.voicePlayer?.playerErrorListener = this
        this.voicePlayer?.messageStateChangeListener = this
    }

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater?.inflate(R.layout.conversation_view, container, false)

        this.messagesListView = view?.findViewById(R.id.messages_list) as MessagesList?

        this.addAttachmentButton = view?.findViewById(R.id.add_attachment_btn) as ImageButton?
        this.addAttachmentButton?.setOnClickListener {
            this@ConversationFragment.onAddAttachmentButtonClick()
        }

        this.messageSendButton = view?.findViewById(R.id.msg_send_btn) as ImageButton?
        this.messageSendButton?.setOnClickListener {
            this@ConversationFragment.onSendMessageButtonClick()
        }

        this.messageEditText = view?.findViewById(R.id.msg_edit_text) as EditText?
        this.messageEditText?.addTextChangedListener(object : TextBaseWatcher() {
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                this@ConversationFragment.onMessageEditTextChanged()
            }
        })

        this.voiceButtonHolderHint = view?.findViewById(R.id.voice_recording_btn_holder_hint)
        this.voiceButtonHolder = view?.findViewById(R.id.voice_recording_btn_holder) as HoldingButtonLayout?
        this.voiceButtonHolder?.addListener(object : HoldingButtonLayoutBaseListener() {
            override fun onExpand() {
                super.onExpand()
                this@ConversationFragment.onVoiceRecordingButtonPressedDown()
            }

            override fun onCollapse(isCancel: Boolean) {
                super.onCollapse(isCancel)
                this@ConversationFragment.onVoiceRecordingButtonPressedUp(isCancel)
            }
        })

        this.arguments?.let { args ->
            args.getString(ConversationBundleKey)?.let { convJson ->
                val conv = ChatConversation.fromJson(JSONObject(convJson))
                this.conversation = Conversation(conv)
            }
        }

        this.activity.title = this.conversation?.dialogName

        this.messageContentTypeChecker = ConversationFragment.ContentTypeChecker()
        val messageHolder = MessageHolders()
                .setOutcomingTextHolder(CustomOutcomingTextMessageViewHolder::class.java)
                .setOutcomingImageHolder(CustomOutcomingImageMessageViewHolder::class.java)
                .setOutcomingImageLayout(R.layout.item_custom_outcoming_image_message)
                .setOutcomingTextLayout(R.layout.item_custom_outcoming_text_message)
                .registerContentType(
                        ConversationFragment.ContentTypeChecker.VoiceMessageType,
                        IncomingVoiceMessageView::class.java, R.layout.item_incoming_voice_message,
                        OutgoingVoiceMessageView::class.java, R.layout.item_outgoing_voice_message,
                        this.messageContentTypeChecker as ConversationFragment.ContentTypeChecker
                )

        this.messagesListAdapter = MessagesListAdapter(
                this.skygear?.auth?.currentUser?.id,
                messageHolder,
                ImageLoader(this.activity)
        )
        this.messagesListView?.setAdapter(this.messagesListAdapter)

        if (this.messagesListView?.layoutManager is LinearLayoutManager) {
            this.messagesListViewReachBottomListener = MessagesListViewReachBottomListener(
                    this.messagesListView?.layoutManager as LinearLayoutManager
            )
            this.messagesListView?.addOnScrollListener(this.messagesListViewReachBottomListener)
        }

        this.messagesListAdapter?.setLoadMoreListener(this)
        this.messagesListAdapter?.setOnMessageClickListener(this)

        this.messagesListAdapter?.setOnMessageClickListener(this)

        // TODO: setup typing indicator subscription

        this.takePhotoPermissionManager = object: PermissionManager(
                this.activity,
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

        this.voiceRecordingPermissionManager = object: PermissionManager(
                this.activity,
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

        return view
    }

    override fun onResume() {
        super.onResume()

        if (this.messagesListAdapter?.itemCount == 0) {
            this.conversation?.let { conv ->
                if (conv.userList.isEmpty()) {
                    conv.chatConversation.participantIds?.let { userIDs ->
                        this.userCache?.getUsers(userIDs.toList()) { users ->
                            conv.userList = users.values.toList()
                        }
                    }
                }

                this.fetchMessages()
            }
        }

        this.messageSubscriptionRetryCount = 0
        this.subscribeMessage()
    }

    override fun onPause() {
        super.onPause()

        this.unsubscribeMessage()
    }

    private fun fetchMessages(
            before: Date? = null,
            complete: ((msgs: List<Message>?, error: String?) -> Unit)? = null
    ) {
        val successCallback = fun(chatMsgs: List<ChatMessage>?) {
            val msgs = chatMsgs?.map { chatMsg -> MessageFactory.getMessage(chatMsg) }
            msgs?.let { this@ConversationFragment.addMessages(it, isAddToTop = true) }
            msgs?.map { it.createdAt }?.min()?.let { newBefore ->
                // update load more cursor
                if (newBefore.before(this@ConversationFragment.messageLoadMoreBefore)) {
                    this@ConversationFragment.messageLoadMoreBefore = newBefore
                }
            }

            complete?.let { it(msgs, null) }
        }

        this.conversation?.let { conv ->
            this.skygearChat?.getMessages(
                    conv.chatConversation,
                    0,
                    before,
                    null,
                    object : GetCallback<List<ChatMessage>> {
                        override fun onSucc(chatMsgs: List<ChatMessage>?)
                                = successCallback(chatMsgs)

                        override fun onFail(failReason: String?) {
                            Log.w(TAG, "Failed to get message: %s".format(failReason))
                            complete?.let { it(null, failReason) }
                        }
                    })
        }
    }

    private fun addMessagesToBottom(msgs: List<Message>) {
        var needScrollToBottom = false
        if (this.messagesListViewReachBottomListener?.isReachEnd == true) {
            needScrollToBottom = true
        }

        this.addMessages(msgs, isScrollToBottom = needScrollToBottom)
    }

    private fun addMessages(msgs: List<Message>,
                            isAddToTop: Boolean = false,
                            isScrollToBottom: Boolean = false
    ) {
        if (msgs.isEmpty()) {
            return
        }

        // fetch user if needed
        val userIDs = msgs.map { it.author?.id ?: it.chatMessage.record.ownerId }
        this.userCache?.let { cache ->
            cache.getUsers(userIDs) { userMap ->
                val multiTypedMessages = msgs.map { originalMsg ->
                    if (VoiceMessage.isVoiceMessage(originalMsg)) {
                        VoiceMessage(originalMsg.chatMessage)
                    } else {
                        originalMsg
                    }
                }.let {
                    it.forEach { msg ->
                        if (msg.chatMessage.record.ownerId != null) {
                            msg.author = userMap[msg.chatMessage.record.ownerId]
                        } else {
                            msg.author = User(this.skygear?.auth?.currentUser!!)
                        }
                        msg
                    }
                    it
                }

                if (isAddToTop) {
                    this.messagesListAdapter?.addToEnd(multiTypedMessages, false)
                } else {
                    multiTypedMessages.forEach { msg ->
                        if (messageIDs.contains(msg.id)) {
                            this@ConversationFragment.messagesListAdapter?.update(msg)
                        } else {
                            this@ConversationFragment.messagesListAdapter?.addToStart(
                                    msg,
                                    isScrollToBottom
                            )
                        }
                    }
                }

            }
        }

        msgs.forEach { msg ->
            messageIDs.add(msg.id)
        }

        // mark last read message
        this.conversation?.chatConversation?.let { conv ->
            val lastChatMsg = msgs.last().chatMessage
            this.skygearChat?.markConversationLastReadMessage(conv, lastChatMsg)
        }

        // mark messages as read
        val chatMsgs = msgs.map { it.chatMessage }
        this.skygearChat?.markMessagesAsRead(chatMsgs)
    }

    private fun updateMessages(msgs: List<Message>) {
        val userIDs = msgs.map { it.chatMessage.record.ownerId }
        this.userCache?.let { cache ->
            cache.getUsers(userIDs) { userMap ->
                msgs.map { msg ->
                    if (VoiceMessage.isVoiceMessage(msg)) {
                        VoiceMessage(msg.chatMessage)
                    } else {
                        msg
                    }
                }.forEach { msg ->
                    msg.author = userMap[msg.chatMessage.record.ownerId]
                    this@ConversationFragment.messagesListAdapter?.update(msg)
                }
            }
        }
    }

    private fun subscribeMessage() {
        if (this.messageSubscriptionRetryCount >= MESSAGE_SUBSCRIPTION_MAX_RETRY) {
            Log.i(TAG, "Message subscription retry has reach the maximum, abort.")
            return
        }
        this.messageSubscriptionRetryCount++
        this.conversation?.chatConversation?.let { conv ->
            this.skygearChat?.subscribeConversationMessage(
                    conv,
                    object : MessageSubscriptionCallback(conv) {
                        override fun notify(eventType: String,
                                            message: ChatMessage
                        ) {
                            when (eventType) {
                                EVENT_TYPE_CREATE ->
                                    this@ConversationFragment.onReceiveChatMessage(MessageFactory.getMessage(message))
                                EVENT_TYPE_UPDATE ->
                                    this@ConversationFragment.onUpdateChatMessage(MessageFactory.getMessage(message))
                            }
                        }

                        override fun onSubscriptionFail(reason: String?) {
                            this@ConversationFragment.subscribeMessage()
                        }
                    })
        }
    }

    private fun unsubscribeMessage() {
        this.conversation?.chatConversation?.let { conv ->
            this.skygearChat?.unsubscribeConversationMessage(conv)
        }
    }

    private fun onReceiveChatMessage(msg: Message) {
        this.addMessagesToBottom(listOf(msg))
    }

    private fun onUpdateChatMessage(msg: Message) {
        this.updateMessages(listOf(msg))
    }

    override fun onLoadMore(page: Int, totalItemsCount: Int) {
        this.fetchMessages(before = this.messageLoadMoreBefore)
    }

    fun onVoiceMessageClick(voiceMessage: VoiceMessage) {
        if (voiceMessage.state == VoiceMessage.State.PLAYING) {
            this.voicePlayer?.pause()
            return
        }

        if (this.voicePlayer?.message != voiceMessage) {
            this.voicePlayer?.stop()
            this.voicePlayer?.message = voiceMessage
        }

        this.voicePlayer?.play()
    }

    override fun onVoiceMessageStateChanged(voiceMessage: VoiceMessage) {
        Log.i(TAG, "Voice Message State Changed: ${voiceMessage.state}")
        this.messagesListAdapter?.update(voiceMessage)
    }

    override fun onVoiceMessagePlayerError(error: VoiceMessagePlayer.Error) {
        Toast.makeText(this.activity, error.message, Toast.LENGTH_SHORT).show()
    }

    fun onAddAttachmentButtonClick() {
        AlertDialog.Builder(this.activity)
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

    fun onSendMessageButtonClick() {
        this.messageEditText?.text?.toString()?.let { msgContent ->
            if (msgContent.isEmpty()) {
                return
            }

            val success = this@ConversationFragment.onSendMessage(msgContent)
            if (success) {
                this@ConversationFragment.messageEditText?.setText("")
            }
        }
    }

    fun onMessageEditTextChanged() {
        this.messageEditText?.text?.let { msgContent ->
            if (msgContent.isEmpty()) {
                this@ConversationFragment.voiceButtonHolder?.visibility = View.VISIBLE
                this@ConversationFragment.messageSendButton?.visibility = View.INVISIBLE
            } else {
                this@ConversationFragment.voiceButtonHolder?.visibility = View.INVISIBLE
                this@ConversationFragment.messageSendButton?.visibility = View.VISIBLE
            }
        }
    }

    fun onVoiceRecordingButtonPressedDown() {
        this.voiceButtonHolderHint?.visibility = View.VISIBLE
        listOf(this.addAttachmentButton, this.messageEditText).map {
            it?.visibility = View.INVISIBLE
        }

        val fileDir = this.activity.cacheDir.absolutePath
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
    }

    fun onVoiceRecordingButtonPressedUp(isCancel: Boolean) {
        this.voiceButtonHolderHint?.visibility = View.INVISIBLE
        listOf(this.addAttachmentButton, this.messageEditText).map {
            it?.visibility = View.VISIBLE
        }

        this.messageEditText?.requestFocus()

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

        val voiceRecordingFile = File(this.voiceRecordingFileName!!)
        val bytes = ByteArray(voiceRecordingFile.length().toInt())
        val stream = BufferedInputStream(FileInputStream(voiceRecordingFile))
        stream.read(bytes, 0, bytes.size)
        stream.close()

        this.conversation?.let { conv ->
            val fileName = this@ConversationFragment.voiceRecordingFileName!!.split("/").last()
            val asset = Asset(fileName, VoiceMessage.MIME_TYPE, bytes)
            val meta = JSONObject()
            meta.put(VoiceMessage.DurationMatadataName, duration)

            this.skygearChat?.sendMessage(
                    conv.chatConversation,
                    null,
                    asset,
                    meta,
                    object : SaveCallback<ChatMessage> {
                        override fun onSucc(chatMsg: ChatMessage?) {
                            voiceRecordingFile.delete()
                        }

                        override fun onFail(failReason: String?) {
                            Log.e(
                                    ConversationFragment.TAG,
                                    "Failed to send voice message: $failReason"
                            )
                        }
                    }
            )
        }
    }

    fun onSendMessage(input: String): Boolean {
        this.conversation?.chatConversation?.let { conv ->
            val message = ChatMessage()
            message.body = input.trim()

            val msg = Message(message)
            msg.author = User(this.skygear?.auth?.currentUser!!)
            this.addMessagesToBottom(listOf(msg))

            this.skygearChat?.addMessage(message, conv, null)
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
        this.conversation?.chatConversation?.let { conv ->
            val imageByteArray = bitmapToByteArray(imageData.image)
            val thumbByteArray = bitmapToByteArray(imageData.thumbnail)

            val meta = JSONObject()
            val encoded = Base64.encodeToString(thumbByteArray, Base64.DEFAULT)
            meta.put("thumbnail", encoded)
            meta.put("height", imageData.image.height)
            meta.put("width", imageData.image.width)

            val message = ChatMessage()
            message.asset = Asset("test.jpg", "image/jpeg", imageByteArray)
            message.metadata = meta

            val msg = ImageMessage(message, imageUri.toString())
            msg.author = User(this.skygear?.auth?.currentUser!!)
            this.addMessagesToBottom(listOf(msg))

            this.skygearChat?.addMessage(message, conv, null)
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
            Toast.makeText(this.activity, msgId, Toast.LENGTH_SHORT).show()
        }
    }

    private fun voiceRecordingPermissionDenied() {
        this.voiceButtonHolder?.cancel()
        Toast.makeText(
                this.activity,
                R.string.please_turn_on_audio_recording_permission,
                Toast.LENGTH_SHORT
        ).show()
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

private abstract class HoldingButtonLayoutBaseListener : HoldingButtonLayoutListener {
    override fun onBeforeCollapse() {}

    override fun onOffsetChanged(offset: Float, isCancel: Boolean) {}

    override fun onBeforeExpand() {}

    override fun onExpand() {}

    override fun onCollapse(isCancel: Boolean) {}
}

private abstract class TextBaseWatcher : TextWatcher {
    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}

/**
 * PermissionManager encapsulates the complicated permission request flow
 */
private abstract class PermissionManager(
        val activityContext: Activity,
        val permissions: List<String>,
        val permissionGrantedHandler: (() -> Unit)? = null,
        val permissionDeniedHandler: ((
                permissionsDenied: List<String>,
                neverAskAgain: Boolean
        ) -> Unit)? = null
) {
    private var permissionsNeverAskAgain = false

    abstract fun request(permissions: List<String>)

    private val deniedPermissions: List<String>
        get() = this.permissions.filter {
            ContextCompat.checkSelfPermission(this.activityContext, it) ==
                    PackageManager.PERMISSION_DENIED
        }

    fun notifyRequestResult(
            permissions: List<String>,
            grantResults: List<Int>
    ) {
        val granted = grantResults.isNotEmpty() &&
                grantResults.first() == PackageManager.PERMISSION_GRANTED
        if (granted) {
            this.permissionGrantedHandler?.invoke()
        } else {
            this.permissionsNeverAskAgain = permissions.filter {
                ActivityCompat.shouldShowRequestPermissionRationale(
                        this@PermissionManager.activityContext,
                        it
                ).not()
            }.any()
            this.permissionDeniedHandler?.invoke(
                    permissions,
                    this.permissionsNeverAskAgain
            )
        }
    }

    fun permissionsGranted() = this.deniedPermissions.isEmpty()

    fun runIfPermissionGranted(toRun: () -> Unit) {
        val permissionsDenied = this.deniedPermissions

        when {
            permissionsDenied.isEmpty() -> toRun()
            this.permissionsNeverAskAgain ->
                this.permissionDeniedHandler
                        ?.invoke(
                                permissionsDenied,
                                this.permissionsNeverAskAgain
                        )
            else -> this.request(permissionsDenied)
        }


    }
}
