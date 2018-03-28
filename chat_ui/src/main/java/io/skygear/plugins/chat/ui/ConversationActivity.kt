package io.skygear.plugins.chat.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class ConversationActivity : AppCompatActivity() {

    companion object {
        @JvmField val ConversationIntentKey = "CONVERSATION"
        @JvmField val ConversationIdIntentKey = "CONVERSATION_ID"
        @JvmField val LayoutIntentKey = "LAYOUT"
        @JvmField val AvatarAdapterIntentKey = "AVATAR_ADAPTER"
        @JvmField val TitleOptionIntentKey = "TITLE_OPTION"
        @JvmField val ConversationViewAdapterIntentKey = "VIEW_ADAPTER"
        @JvmField val MessageSentListenerIntentKey = "MESSAGE_SENT_LISTENER"
        @JvmField val MessageFetchListenerIntentKey = "MESSAGE_FETCH_LISTENER"
        @JvmField val ConversationFetchListenerIntentKey = "CONVERSATION_FETCH_LISTENER"
        @JvmField val ConnectionListenerIntentKey = "CONNECTION_LISTENER"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_conversation)

        if (savedInstanceState == null) {
            val fragment = ConversationFragment()
            val bundle = Bundle()
            this.intent?.getStringExtra(ConversationActivity.ConversationIdIntentKey)?.let { conversationId ->
                bundle.putString(ConversationFragment.ConversationIdBundleKey, conversationId)
            }

            this.intent?.getStringExtra(ConversationActivity.ConversationIntentKey)?.let { convJson ->
                bundle.putString(ConversationFragment.ConversationBundleKey, convJson)
            }

            intent?.getIntExtra(LayoutIntentKey, -1)?.let { value ->
                if (value > 0) {
                    bundle.putInt(ConversationFragment.LayoutResIdBundleKey, value)
                }
            }

            intent?.getSerializableExtra(AvatarAdapterIntentKey)?.let { adapter ->
                bundle.putSerializable(ConversationFragment.AvatarAdapterBundleKey,
                        adapter)
            }

            intent?.getSerializableExtra(MessageSentListenerIntentKey)?.let { adapter ->
                bundle.putSerializable(ConversationFragment.MessageSentListenerKey,
                        adapter)
            }

            intent?.getSerializableExtra(MessageFetchListenerIntentKey)?.let { listener ->
                bundle.putSerializable(ConversationFragment.MessageFetchListenerKey,
                        listener)
            }

            intent?.getSerializableExtra(ConversationFetchListenerIntentKey)?.let { listener ->
                bundle.putSerializable(ConversationFragment.ConversationFetchListenerKey,
                        listener)
            }

            intent?.getSerializableExtra(TitleOptionIntentKey)?.let { titleOption ->
                bundle.putSerializable(ConversationFragment.TitleOptionBundleKey,
                        titleOption)
            }

            intent?.getSerializableExtra(ConnectionListenerIntentKey)?.let { listener ->
                bundle.putSerializable(ConversationFragment.ConnectionListenerKey,
                        listener)
            }

            intent?.getSerializableExtra(ConversationViewAdapterIntentKey)?.let { adapter ->
                bundle.putSerializable(ConversationFragment.ConversationViewAdapterBundleKey,
                        adapter)
            }

            fragment.arguments = bundle

            this.supportFragmentManager
                    .beginTransaction()
                    .add(R.id.activity_conversation, fragment)
                    .commit()
        }
    }
}
