package io.skygear.plugins.chat.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.Conversation as ChatConversation


class ConversationActivity : AppCompatActivity() {

    companion object {
        @JvmField val ConversationIntentKey = "CONVERSATION"
        @JvmField val LayoutIntentKey = "LAYOUT"
        @JvmField val AvatarAdapterIntentKey = "AVATAR_ADAPTER"
        @JvmField val MessageSentListenerIntentKey = "MESSAGE_SENT_LISTENER"
        @JvmField val MessageFetchListenerIntentKey = "MESSAGE_FETCH_LISTENER"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_conversation)

        if (savedInstanceState == null) {
            val fragment = ConversationFragment()
            this.intent?.getStringExtra(ConversationActivity.ConversationIntentKey)?.let { convJson ->
                val bundle = Bundle()
                bundle.putString(ConversationFragment.ConversationBundleKey, convJson)
                if (intent?.hasExtra(LayoutIntentKey) ?: false) {
                    bundle.putInt(ConversationFragment.LayoutResIdBundleKey,
                                  this.intent?.getIntExtra(LayoutIntentKey, -1) !!)
                }
                if (intent?.hasExtra(AvatarAdapterIntentKey) ?: false) {
                    bundle.putSerializable(ConversationFragment.AvatarAdapterBundleKey,
                            this.intent?.getSerializableExtra(AvatarAdapterIntentKey))
                }
                if (intent?.hasExtra(MessageSentListenerIntentKey) ?: false) {
                    bundle.putSerializable(ConversationFragment.MessageSentListenerKey,
                            this.intent?.getSerializableExtra(MessageSentListenerIntentKey))
                }
                if (intent?.hasExtra(MessageFetchListenerIntentKey) ?: false) {
                    bundle.putSerializable(ConversationFragment.MessageFetchListenerKey,
                            this.intent?.getSerializableExtra(MessageFetchListenerIntentKey))
                }

                fragment.arguments = bundle
            }

            this.supportFragmentManager
                    .beginTransaction()
                    .add(R.id.activity_conversation, fragment)
                    .commit()
        }
    }
}
