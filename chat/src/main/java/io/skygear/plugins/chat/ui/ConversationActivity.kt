package io.skygear.plugins.chat.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.Conversation as ChatConversation

class ConversationActivity : AppCompatActivity() {

    companion object {
        @JvmField open val ConversationIntentKey = "CONVERSATION"
        @JvmField open val LayoutIntentKey = "LAYOUT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_conversation)

        if (savedInstanceState == null) {
            val layoutId = this.intent?.getIntExtra(LayoutIntentKey, R.layout.conversation_fragment)
            val fragment = ConversationFragment(layoutId)
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
