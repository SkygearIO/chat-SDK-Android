package io.skygear.chatexample

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.EditText
import io.skygear.plugins.chat.*
import io.skygear.skygear.Asset
import io.skygear.skygear.Container
import java.io.ByteArrayInputStream
import java.lang.ref.WeakReference
import java.net.URLConnection
import java.util.*

class ConversationActivity : AppCompatActivity() {
    private val LOG_TAG = "ConversationActivity"
    private val MESSAGES_LIMIT = 25
    private val SELECT_SINGLE_PICTURE = 101

    private val mSkygear: Container
    private val mChatContainer: ChatContainer
    private var mConversationId: String? = null
    private val mAdapter: ConversationAdapter = ConversationAdapter()
    private var mConversationRv: RecyclerView? = null
    private var mInputEt: EditText? = null
    private var mLoading: ProgressDialog? = null
    private var mAsset: Asset? = null

    companion object {
        private val ID_KEY = "id_key"

        fun newIntent(conversation: Conversation, context: Context): Intent {
            val i = Intent(context, ConversationActivity::class.java)
            i.putExtra(ID_KEY, conversation.id)

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

        mConversationId = intent.getStringExtra(ID_KEY)

        mConversationRv = findViewById(R.id.conversation_rv) as RecyclerView
        mConversationRv?.adapter = mAdapter
        mConversationRv?.layoutManager = LinearLayoutManager(this)

        mInputEt = findViewById(R.id.input_et) as EditText
        val sendBtn = findViewById(R.id.send_btn)
        val attachmentBtn = findViewById(R.id.attachment_btn)

        sendBtn.setOnClickListener { send() }
        attachmentBtn.setOnClickListener { findAttachment() }

        mLoading = ProgressDialog(this)
        mLoading?.setTitle(R.string.loading)
        mLoading?.setMessage(getString(R.string.attaching))
    }

    override fun onResume() {
        super.onResume()

        mChatContainer.getAllMessages(mConversationId!!, MESSAGES_LIMIT, Date(),
                object : GetCallback<List<Message>> {
                    override fun onSucc(list: List<Message>?) {
                        mAdapter.setMessages(list)
                        if (list != null && !list.isEmpty()) {
                            mChatContainer.markConversationLastReadMessage(
                                    mConversationId!!, list.first().id)
                        }
                    }

                    override fun onFail(failReason: String?) {

                    }
                })

        mChatContainer.subConversationMessage(mConversationId!!, { t, m ->
            when (t) {
                "create" -> mAdapter.addMessage(m)
            }
        })
    }

    override fun onPause() {
        super.onPause()

        mChatContainer.unSubConversationMessage(mConversationId!!)
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
            mChatContainer.sendMessage(mConversationId!!,
                    body.toString().trim(),
                    mAsset,
                    null,
                    object : SaveCallback<Message> {
                        override fun onSucc(m: Message?) {

                        }

                        override fun onFail(failReason: String?) {

                        }
                    }
            )
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

    class AssetWorker(val uri: Uri, val contentResolver: ContentResolver, val weakRef: WeakReference<ConversationActivity>) : AsyncTask<Void, Void, Asset>() {
        val LOG_TAG = "AssetWorker"

        override fun doInBackground(vararg params: Void?): Asset? {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = getBytes()
            val mime = URLConnection.guessContentTypeFromStream(ByteArrayInputStream(bytes))

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
