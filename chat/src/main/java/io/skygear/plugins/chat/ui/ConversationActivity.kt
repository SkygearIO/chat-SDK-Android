package io.skygear.plugins.chat.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.model.Conversation
import org.json.JSONObject
import io.skygear.plugins.chat.Conversation as ChatConversation

class ConversationActivity : AppCompatActivity() {

    companion object {
        val ConversationIntentKey = "CONVERSATION"
        private val TAG = "ConversationActivity"
    }

    var conversationFragment: ConversationFragment? = null

    private var conversation: Conversation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_conversation)

        if (savedInstanceState == null) {
            val fragment = ConversationFragment()
            this.intent?.getStringExtra(ConversationActivity.ConversationIntentKey)?.let { convJson ->
                val bundle = Bundle()
                bundle.putString(ConversationFragment.ConversationBundleKey, convJson)
                fragment.arguments = bundle
            }

            this.supportFragmentManager
                    .beginTransaction()
                    .add(R.id.activity_conversation, fragment)
                    .commit()
        }
    }
}
