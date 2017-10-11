package io.skygear.plugins.chat.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.skygear.plugins.chat.ChatContainer
import io.skygear.plugins.chat.GetCallback
import io.skygear.plugins.chat.MessageSubscriptionCallback
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.model.Conversation
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.utils.ImageLoader
import io.skygear.plugins.chat.ui.utils.UserCache
import io.skygear.skygear.Container
import org.json.JSONObject
import io.skygear.plugins.chat.Conversation as ChatConversation
import io.skygear.plugins.chat.Message as ChatMessage

class ConversationFragment : Fragment(),
        MessageInput.InputListener,
        MessageInput.AttachmentsListener
{
    companion object {
        val ConversationBundleKey = "CONVERSATION"
        private val TAG = "ConversationFragment"
        private val MESSAGE_SUBSCRIPTION_MAX_RETRY = 10
    }

    var conversation: Conversation? = null

    private var messagesListView: MessagesList? = null
    private var messageInput: MessageInput? = null

    private var skygear: Container? = null
    private var skygearChat: ChatContainer? = null
    private var userCache: UserCache? = null
    private var messagesListAdapter: MessagesListAdapter<Message>? = null
    private var messagesListViewReachBottomListener: MessagesListViewReachBottomListener? = null

    private var messageSubscriptionRetryCount = 0

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        this.skygear = Container.defaultContainer(context)
        this.skygearChat = ChatContainer.getInstance(this.skygear as Container)
        this.userCache = UserCache.getInstance(
                this.skygear as Container,
                this.skygearChat as ChatContainer
        )
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

        this.activity.title = this.conversation?.dialogName

        this.messagesListAdapter = MessagesListAdapter(
                this.skygear?.auth?.currentUser?.id,
                ImageLoader(this.activity)
        )
        this.messagesListView?.setAdapter(this.messagesListAdapter)

        if (this.messagesListView?.layoutManager is LinearLayoutManager) {
            this.messagesListViewReachBottomListener = MessagesListViewReachBottomListener(
                    this.messagesListView?.layoutManager as LinearLayoutManager
            )
            this.messagesListView?.addOnScrollListener(this.messagesListViewReachBottomListener)
        }

        // TODO: Set On Load More Listener
        // TODO: setup typing indicator subscription

        this.messageInput?.setInputListener(this)
        this.messageInput?.setAttachmentsListener(this)

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

                this.skygearChat?.getMessages(
                        conv.chatConversation,
                        0,
                        null,
                        null,
                        object : GetCallback<List<ChatMessage>> {
                            override fun onSucc(chatMsgs: List<ChatMessage>?) {
                                chatMsgs?.map { chatMsg -> Message(chatMsg) }?.let { msgs ->
                                    this@ConversationFragment.addMessages(msgs, isAddToTop = true)
                                }
                            }

                            override fun onFail(failReason: String?) {
                                Log.w(TAG, "Failed to get message: %s".format(failReason))
                            }
                        })
            }
        }

        this.messageSubscriptionRetryCount = 0
        this.subscribeMessage()
    }

    override fun onPause() {
        super.onPause()

        this.unsubscribeMessage()
    }

    private fun addMessages(msgs: List<Message>,
                            isAddToTop: Boolean = false,
                            isScrollToBottom: Boolean = false
    ) {
        if (msgs.isEmpty()) {
            return
        }

        // fetch user if needed
        val userIDs = msgs.map { it.chatMessage.record.ownerId }
        this.userCache?.let { cache ->
            cache.getUsers(userIDs) { userMap ->
                msgs.forEach { msg ->
                    msg.author = userMap[msg.chatMessage.record.ownerId]
                }

                if (isAddToTop) {
                    this.messagesListAdapter?.addToEnd(msgs, false)
                } else {
                    msgs.forEach { msg ->
                        this@ConversationFragment.messagesListAdapter?.addToStart(
                                msg,
                                isScrollToBottom
                        )
                    }
                }

            }
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
                msgs.forEach { msg ->
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
                                    this@ConversationFragment.onReceiveChatMessage(Message(message))
                                EVENT_TYPE_UPDATE ->
                                    this@ConversationFragment.onUpdateChatMessage(Message(message))
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
        var needScrollToBottom = false
        if (this.messagesListViewReachBottomListener?.isReachEnd == true) {
            needScrollToBottom = true
        }

        this.addMessages(listOf(msg), isScrollToBottom = needScrollToBottom)
    }

    private fun onUpdateChatMessage(msg: Message) {
        this.updateMessages(listOf(msg))
    }

    // implement MessageInput.AttachmentsListener
    override fun onAddAttachments() {
        // TODO: add attachment
    }

    // implement MessageInput.InputListener
    override fun onSubmit(input: CharSequence?): Boolean {
        this.conversation?.chatConversation?.let { conv ->
            this.skygearChat?.sendMessage(
                    conv,
                    input?.toString()?.trim(),
                    null,
                    null,
                    null
            )
        }

        return true
    }

}

private class MessagesListViewReachBottomListener(
        private val layoutManager: LinearLayoutManager
) : RecyclerView.OnScrollListener()
{
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
