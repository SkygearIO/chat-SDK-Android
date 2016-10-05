package io.skygear.chatexample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.skygear.plugins.chat.ChatContainer
import io.skygear.plugins.chat.Conversation
import io.skygear.skygear.Container
import java.util.*

class ConversationActivity : AppCompatActivity() {
    private val LOG_TAG = "ConversationActivity"
    private val MESSAGES_LIMIT = 25

    private var mSkygear: Container? = null
    private var mChatMgr: ChatContainer? = null
    private var mConversationId: String? = null
    private val mAdapter: ConversationAdapter = ConversationAdapter()
    private var mConversationRv: RecyclerView? = null

    companion object {
        private val ID_KEY = "id_key"

        fun newIntent(conversation: Conversation, context: Context): Intent {
            val i = Intent(context, ConversationActivity::class.java)
            i.putExtra(ID_KEY, conversation.id)

            return i
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        mSkygear = Container.defaultContainer(this)
        mChatMgr = ChatContainer.getInstance(mSkygear)
        mConversationId = intent.getStringExtra(ID_KEY)

        mConversationRv = findViewById(R.id.conversation_rv) as RecyclerView
        mConversationRv?.adapter = mAdapter
        mConversationRv?.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()

        mChatMgr?.getMessages(mConversationId, MESSAGES_LIMIT, Date(), { list, s ->
            mAdapter.setMessages(list)
        })
    }
}
