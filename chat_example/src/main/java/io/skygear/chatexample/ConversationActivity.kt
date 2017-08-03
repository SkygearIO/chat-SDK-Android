package io.skygear.chatexample

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import io.skygear.plugins.chat.*
import io.skygear.skygear.Asset
import io.skygear.skygear.Container
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.*

class ConversationActivity : AppCompatActivity() {
    private val TAG = "ConversationActivity"
    private val MESSAGES_LIMIT = 25
    private val TYPING_CHECKER_INTERVAL = 1000L
    private val SELECT_SINGLE_PICTURE = 101
    private val MESSAGE_SUBSCRIPTION_MAX_RETRY = 10
    private val TYPING_SUBSCRIPTION_MAX_RETRY = 10

    private val mSkygear: Container
    private val mChatContainer: ChatContainer
    private var mConversation: Conversation? = null
    private val mAdapter: ConversationAdapter = ConversationAdapter()
    private var mAsset: Asset? = null

    private val mTypingStates = HashMap<String, Typing.State>()
    private var mMessageSubscriptionRetryCount = 0
    private var mTypingSubscriptionRetryCount = 0

    private var mConversationRv: RecyclerView? = null
    private var mInputEt: EditText? = null
    private var mLoading: ProgressDialog? = null
    private var mTypingIndicatorTextView: TextView? = null

    private var mInputChanged = false
    private var mTypingCheckerHandler = Handler()
    private var mTypingCheckerTask : Runnable? = null

    companion object {
        private val CONVERSATION_KEY         = "conversation_key"
        private val UNREAD_COUNT_KEY         = "unread_count_key"
        private val LAST_READ_MESSAGE_ID_KEY = "last_read_message_id_key"

        fun newIntent(conversation: Conversation, context: Context): Intent {
            val i = Intent(context, ConversationActivity::class.java)
            val serializedConversation = conversation.toJson().toString()
            i.putExtra(CONVERSATION_KEY, serializedConversation)
            i.putExtra(UNREAD_COUNT_KEY, conversation.unreadCount)
            i.putExtra(LAST_READ_MESSAGE_ID_KEY, conversation.lastReadMessageId)
            return i
        }
    }

    init {
        mSkygear = Container.defaultContainer(this)
        mChatContainer = ChatContainer.getInstance(mSkygear)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        val serializedConversation   = intent.getStringExtra(CONVERSATION_KEY)
        val unreadCount              = intent.getIntExtra(UNREAD_COUNT_KEY, 0)
        val lastReadMessageIdKey     = intent.getStringExtra(LAST_READ_MESSAGE_ID_KEY)
        mConversation = Conversation.fromJson(JSONObject(serializedConversation))

        mConversationRv = findViewById(R.id.conversation_rv) as RecyclerView
        mConversationRv?.adapter = mAdapter
        mConversationRv?.layoutManager = LinearLayoutManager(this)

        mInputEt = findViewById(R.id.input_et) as EditText
        setupTypingChecker()

        val sendBtn = findViewById(R.id.send_btn)
        val attachmentBtn = findViewById(R.id.attachment_btn)

        sendBtn?.setOnClickListener { send() }
        attachmentBtn?.setOnClickListener { findAttachment() }

        mLoading = ProgressDialog(this)
        mLoading?.setTitle(R.string.loading)
        mLoading?.setMessage(getString(R.string.attaching))

        mTypingIndicatorTextView = findViewById(R.id.typing_indicator_tv) as TextView
        mTypingIndicatorTextView?.text = ""

        mAdapter.setOnClickListener {
            m -> showOptions(m)
        }
    }

    override fun onResume() {
        super.onResume()

        mMessageSubscriptionRetryCount = 0
        mTypingSubscriptionRetryCount = 0

        startTypingChecker()
        getAllMessages()
        subscribeMessage()
        subscribeTypingIndicator()

    }

    override fun onPause() {
        super.onPause()

        stopTypingChecker()
        sendTypingState(Typing.State.FINISHED)

        mChatContainer.unsubscribeConversationMessage(mConversation!!)
        mChatContainer.unsubscribeTypingIndicator(mConversation!!)
    }

    fun onReceiveInitialMessages(messages: List<Message>?) {
        mAdapter.setMessages(messages)
        markLatestMessageAsLastRead()
        if (messages != null && !messages.isEmpty()) {
            mChatContainer.markMessagesAsRead(messages)
        }
    }

    fun onReceiveMessage(message: Message) {
        mAdapter.addMessage(message)
        markLatestMessageAsLastRead()
        mChatContainer.markMessageAsRead(message)
    }

    fun markLatestMessageAsLastRead()
    {
        val lastMessage = mAdapter.getLatestMessage()
        if (lastMessage != null) {
            mChatContainer.markConversationLastReadMessage(mConversation!!, lastMessage)
        }
    }

    fun onUpdateMessage(message: Message) {
        mAdapter.updateMessage(message)
    }

    fun updateTypingIndicatorTextView() {
        val currentUserId = mSkygear.auth.currentUser.id
        val typingUserCount = mTypingStates
                .filter { it.key != currentUserId && it.value == Typing.State.BEGIN }
                .size
        mTypingIndicatorTextView?.text =
                when (typingUserCount) {
                    0 -> ""
                    1 -> "Someone is typing..."
                    else -> "$typingUserCount users is typing..."
                }
    }


    private fun setupTypingChecker() {
        mTypingCheckerTask = Runnable {
            if (mInputChanged) {
                mInputChanged = false
                sendTypingState(Typing.State.BEGIN)
            } else {
                sendTypingState(Typing.State.FINISHED)
            }
            mTypingCheckerHandler.postDelayed(mTypingCheckerTask, TYPING_CHECKER_INTERVAL)
        }
        mInputEt?.addTextChangedListener( object : TextEditWatcher() {
            override fun afterTextChanged(s: Editable?) {
                mInputChanged = true
            }
        })
    }

    private fun startTypingChecker() {
        mTypingCheckerTask?.run()
    }

    private fun stopTypingChecker() {
        mTypingCheckerHandler.removeCallbacks(mTypingCheckerTask)
    }

    private fun sendTypingState(state : Typing.State) {
        val currentUserId = mSkygear.auth.currentUser.id
        val currentUserState = mTypingStates[currentUserId] ?: Typing.State.FINISHED
        if (currentUserState != state) {
            mChatContainer.sendTypingIndicator(mConversation!!, state)
        }
    }

    private fun getAllMessages() {
        mChatContainer.getMessages(mConversation!!, MESSAGES_LIMIT, Date(),
                object : GetCallback<List<Message>> {
                    override fun onSucc(list: List<Message>?) {
                        onReceiveInitialMessages(list)
                    }

                    override fun onFail(failReason: String?) {
                        Log.w(TAG, "Fail to get message: " + failReason)
                    }
                })
    }

    private fun subscribeMessage() {
        if (mMessageSubscriptionRetryCount >= MESSAGE_SUBSCRIPTION_MAX_RETRY) {
            Log.i(TAG, "Message subscription retry has reach the maximum, abort.")
            return
        }
        mMessageSubscriptionRetryCount++
        mChatContainer.subscribeConversationMessage(
                mConversation!!,
                object : MessageSubscriptionCallback(mConversation!!) {
                    override fun notify(eventType: String, message: Message) {
                        when (eventType) {
                            EVENT_TYPE_CREATE -> onReceiveMessage(message)
                            EVENT_TYPE_UPDATE -> onUpdateMessage(message)
                        }
                    }

                    override fun onSubscriptionFail(reason: String?) {
                        subscribeMessage()
                    }
                }
        )
    }

    private fun subscribeTypingIndicator() {
        if (mTypingSubscriptionRetryCount >= TYPING_SUBSCRIPTION_MAX_RETRY) {
            Log.i(TAG, "Typing subscription retry has reach the maximum, abort.")
            return
        }
        mTypingSubscriptionRetryCount++
        mChatContainer.subscribeTypingIndicator(
                mConversation!!,
                object : TypingSubscriptionCallback(mConversation!!) {
                    override fun notify(typingMap: Map<String, Typing>) {
                        typingMap.forEach { mTypingStates.put(it.key, it.value.state) }
                        updateTypingIndicatorTextView()
                    }

                    override fun onSubscriptionFail(reason: String?) {
                        subscribeTypingIndicator()
                    }
                }
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            SELECT_SINGLE_PICTURE -> addAttachment(resultCode, data)
        }
    }

    fun send() {
        val body = mInputEt?.text

        if ((!body.isNullOrEmpty() && !body.isNullOrBlank())
                || mAsset != null) {
            val asset = mAsset
            mAsset = null
            mChatContainer.sendMessage(mConversation!!, body.toString().trim(), asset, null, null)
            mInputEt?.text?.clear()
        }
    }

    fun findAttachment() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.select_image)),
                SELECT_SINGLE_PICTURE)
    }

    fun addAttachment(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                mLoading?.show()
                AssetWorker(selectedImageUri, contentResolver, WeakReference(this)).execute()
            }
        }
    }

    fun setAsset(asset: Asset?) {
        mAsset = asset
        mLoading?.dismiss()
    }

    fun showOptions(m: Message) {
            val builder = AlertDialog.Builder(this)
            val items = resources.getStringArray(R.array.message_options)
            builder.setItems(items, { d, i -> when(i) {
                0 -> editMessage(m)
                1 -> deleteMessage(m)
            } })
            val alert = builder.create()
            alert.show()
     }

    fun editMessage(m: Message) {
        val builder = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.maxLines = 1
        editText.minLines = 1
        editText.gravity = Gravity.TOP or Gravity.LEFT
        editText.setText(m.body)

        builder.setView(editText)
        builder.setPositiveButton(R.string.yes) {dialog, which ->
            mChatContainer.editMessage(m, editText.text.toString(), object: SaveCallback<Message> {
                override fun onSucc(message: Message?) {
                    if (message != null) {
                        mAdapter.updateMessage(message)
                    }
                }

                override fun onFail(failReason: String?) {
                    Log.w(TAG, "Fail to edit message: " + failReason)
                }
            })
        }
        builder.show()
    }

    fun deleteMessage(m: Message) {
        Log.w(TAG, "Delete a message")
        mChatContainer.deleteMessage(m, object : DeleteCallback<Message> {
            override fun onSucc(message: Message) {
                mAdapter.deleteMessage(message)
            }

            override fun onFail(failReason: String?) {
                Log.w(TAG, "Fail to delete message: " + failReason)
            }
        })
    }

    abstract class TextEditWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // do nothing
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // do nothing
        }
    }

    class AssetWorker(val uri: Uri, val contentResolver: ContentResolver, val weakRef: WeakReference<ConversationActivity>) : AsyncTask<Void, Void, Asset>() {
        val LOG_TAG = "AssetWorker"

        override fun doInBackground(vararg params: Void?): Asset? {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = getBytes()
            val mime = contentResolver.getType(uri)

            if (inputStream != null && bytes != null) {
                return Asset(UUID.randomUUID().toString(), mime, bytes)
            } else {
                return null
            }
        }

        override fun onPostExecute(result: Asset?) {
            weakRef.get()?.setAsset(result)
        }

        fun getBytes(): ByteArray? {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream.readBytes(DEFAULT_BUFFER_SIZE)
            inputStream.close()

            return bytes
        }
    }
}
