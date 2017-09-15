package io.skygear.plugins.chat.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.skygear.plugins.chat.ChatContainer
import io.skygear.plugins.chat.Conversation as ChatConversation
import io.skygear.plugins.chat.GetCallback
import io.skygear.plugins.chat.Message as ChatMessage
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.model.Conversation
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.skygear.Container
import org.json.JSONObject

class ConversationFragment : Fragment() {

    companion object {
        val ConversationBundleKey = "CONVERSATION"
        private val TAG = "ConversationFragment"
    }

    var conversation: Conversation? = null

    private var messagesListView: MessagesList? = null
    private var messageInput: MessageInput? = null

    private var skygear: Container? = null
    private var skygearChat: ChatContainer? = null
    private var messagesListAdapter: MessagesListAdapter<Message>? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        this.skygear = Container.defaultContainer(context)
        this.skygearChat = ChatContainer.getInstance(this.skygear as Container)
    }

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater?.inflate(R.layout.conversation_view, container, false)

        this.messagesListView = view?.findViewById(R.id.messages_list) as MessagesList?
        this.messageInput = view?.findViewById(R.id.message_input) as MessageInput?

        this.arguments?.let { args ->
            args.getString(ConversationBundleKey)?.let { convJson ->
                val conv = ChatConversation.fromJson(JSONObject(convJson))
                this.conversation = Conversation(conv)
            }
        }

        // TODO: add image loader
        this.messagesListAdapter =
                MessagesListAdapter<Message>(this.skygear?.auth?.currentUser?.id, null)
        this.messagesListView?.setAdapter(this.messagesListAdapter)

        return view
    }

    override fun onStart() {
        super.onStart()

        this.conversation?.chatConversation?.let { conv ->
            this.skygearChat?.getMessages(
                    conv,
                    0,
                    null,
                    null,
                    object : GetCallback<List<ChatMessage>> {
                        override fun onSucc(chatMsgs: List<ChatMessage>?) {
                            chatMsgs?.map { chatMsg -> Message(chatMsg) }?.let { msgs ->
                                this@ConversationFragment.messagesListAdapter?.addToEnd(msgs, false)
                            }
                        }

                        override fun onFail(failReason: String?) {
                            Log.w(TAG, "Failed to get message: %s".format(failReason))
                        }
                    })
        }
    }
}