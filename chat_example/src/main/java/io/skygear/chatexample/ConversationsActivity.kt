package io.skygear.chatexample

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.skygear.plugins.chat.* // ktlint-disable no-wildcard-imports
import io.skygear.plugins.chat.ui.* // ktlint-disable no-wildcard-imports
import io.skygear.skygear.Container
import io.skygear.skygear.Error
import io.skygear.skygear.LambdaResponseHandler
import io.skygear.skygear.LogoutResponseHandler
import org.json.JSONObject

class ConversationsActivity : AppCompatActivity() {
    private val LOG_TAG: String? = "ConversationsActivity"
    private val PAGE_SIZE = 10
    private val PAGING_THRESHOLD = 3

    private val mSkygear: Container = Container.defaultContainer(this)
    private val mChatContainer: ChatContainer = ChatContainer.getInstance(mSkygear)
    private val mAdapter: ConversationsAdapter = ConversationsAdapter()
    private var mConversationsRv: RecyclerView? = null
    private var mCurrentPage: Int = 0
    private var mGettingConversation = false
    private var mNoMoreConversations = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        findViewById<RecyclerView>(R.id.conversations_rv)?.let { rv ->
            rv.adapter = mAdapter
            rv.layoutManager = LinearLayoutManager(this)
            rv.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    (recyclerView?.layoutManager as? LinearLayoutManager)?.
                            let { llm ->
                                val itemCount = llm.itemCount
                                val lastItemPos = llm.findLastVisibleItemPosition()

                                this@ConversationsActivity.onRecyclerViewScrolled(
                                        lastItemPos,
                                        itemCount
                                )
                            }


                }
            })

            mConversationsRv = rv
        }

        mAdapter.setOnClickListener {
            pos -> showOptions(pos)
        }
    }



    override fun onResume() {
        super.onResume()

        mChatContainer.subscribeToConversation(object: ConversationSubscriptionCallback() {
            override fun notify(eventType: String, conversation: Conversation) {
                when (eventType) {
                    ConversationSubscriptionCallback.EVENT_TYPE_DELETE ->
                        mAdapter.deleteConversation(conversation.id)

                    ConversationSubscriptionCallback.EVENT_TYPE_UPDATE ->
                        mAdapter.updateConversation(conversation)

                    ConversationSubscriptionCallback.EVENT_TYPE_CREATE ->
                        mAdapter.addConversation(conversation)
                }
            }

            override fun onSubscriptionFail(error: Error) {
                Log.w(LOG_TAG, "Subscription Error: ${error.detailMessage}")
            }
        })

        this.showGreeting()
        this.getConversations()
    }

    override fun onPause() {
        mChatContainer.unsubscribeFromConversation()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.conversation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.log_out_menu -> {
                confirmLogOut()
                true
            }
            R.id.add_conversation_menu -> {
                startActivity(Intent(this, CreateConversationActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onRecyclerViewScrolled(lastVisiableItemPosition: Int, totalItemCount: Int) {
        if (totalItemCount <= 0) {
            return
        }

        if (lastVisiableItemPosition + PAGING_THRESHOLD > totalItemCount) {
            getConversations(mCurrentPage + 1)
        }
    }

    fun showGreeting() {
        val username = mSkygear.auth.currentUser.get("username").toString()
        val greetUserMessage = String.format("Hi, %s!", username)
        Toast.makeText(this@ConversationsActivity, greetUserMessage, Toast.LENGTH_SHORT).show()
    }

    fun getConversations(page: Int = 1) {
        if (mGettingConversation) {
            return
        }

        if (page == 1) {
            mNoMoreConversations = false
        } else if (mNoMoreConversations) {
            return
        }

        mGettingConversation = true

        Log.i(LOG_TAG, "Getting conversations on Page $page")

        mChatContainer.getConversations(
                page,
                PAGE_SIZE,
                true,
                object : GetCallback<List<Conversation>> {
                    override fun onSuccess(list: List<Conversation>?) {
                        mGettingConversation = false

                        if (list?.isEmpty() != false) {
                            if (page != 1) {
                                mNoMoreConversations = true
                            }

                            val msg = if (page == 1) {
                                "No conversations. You may create one with other users."
                            } else {
                                "No more conversations."
                            }

                            Toast.makeText(
                                    this@ConversationsActivity,
                                    msg,
                                    Toast.LENGTH_SHORT
                            ).show()

                            return
                        }

                        val listSize: Int = list.size
                        val message = String.format("%d conversations loaded.", listSize)
                        Toast.makeText(
                                this@ConversationsActivity,
                                message,
                                Toast.LENGTH_SHORT
                        ).show()


                        mCurrentPage = page

                        if (page == 1) {
                            mAdapter.setConversations(list)
                        } else {
                            mAdapter.addConversations(list)
                        }
                    }

                    override fun onFail(error: Error) {
                        mGettingConversation = false

                        Toast.makeText(
                                this@ConversationsActivity,
                                "Failed to get conversations: ${error.detailMessage}",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        )
    }

    fun refreshConversation(id: String) {
        mChatContainer.getConversation(id, object: GetCallback<Conversation> {
            override fun onSuccess(freshConv: Conversation?) {
                mAdapter.updateConversation(freshConv)
            }

            override fun onFail(error: Error) {
                Toast.makeText(
                        this@ConversationsActivity,
                        "Failed to refresh conversation: ${error.detailMessage}",
                        Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun confirmLogOut() {
        AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.are_your_sure_to_log_out)
                .setPositiveButton(R.string.yes) { dialog, which -> logOut() }
                .setNegativeButton(R.string.no, null).show()
    }

    fun logOut() {
        val loading = ProgressDialog(this)
        loading.setTitle(R.string.loading)
        loading.setMessage(getString(R.string.logging_out))
        loading.show()

        mSkygear.auth.logout(object : LogoutResponseHandler() {
            override fun onLogoutSuccess() {
                loading.dismiss()
                backToMain()
            }

            override fun onLogoutFail(error: Error) {
                loading.dismiss()
                backToMain()
            }
        })
    }

    fun backToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


    fun showOptions(pos: Int) {
        val builder = AlertDialog.Builder(this)

        val items = resources.getStringArray(R.array.conversation_options)

        val c : Conversation = mAdapter.getConversation(pos)
        builder.setItems(items, { d, i -> when(i) {

            0 -> enter(c)
            1 -> viewMeta(c)
            2 -> edit(c)
            3 -> confirmLeave(c)
            4 -> confirmDelete(c)
            5 -> updateAdmins(c)
            6 -> updateParticipants(c)
        } })
        val alert = builder.create()
        alert.show()
    }

    fun enter(c: Conversation) {
        val i = Intent(this, ConversationActivity::class.java)
        i.putExtra(ConversationActivity.ConversationIntentKey, c.toJson().toString())
        i.putExtra(ConversationActivity.ConnectionListenerIntentKey, object : ConnectionListener {
            override fun onClose(fragment: ConversationFragment) {
                Toast.makeText(fragment.activity, "Connection Closed", Toast.LENGTH_LONG).show()
            }

            override fun onError(fragment: ConversationFragment, e: Exception?) {
                Toast.makeText(fragment.activity, "Connection Error", Toast.LENGTH_LONG).show()
            }

            override fun onOpen(fragment: ConversationFragment) {
                Toast.makeText(fragment.activity, "Connection Open", Toast.LENGTH_LONG).show()
            }
        })
        i.putExtra(ConversationActivity.MessageSentListenerIntentKey, object : MessageSentListener {
            override fun onBeforeMessageSent(fragment: ConversationFragment, message: Message) {
            }

            override fun onMessageSentSuccess(fragment: ConversationFragment, message: Message) {
                Toast.makeText(fragment.activity, "Message Sent", Toast.LENGTH_LONG).show()
            }

            override fun onMessageSentFailed(fragment: ConversationFragment, message: Message?, error: Error) {
                Toast.makeText(fragment.activity, "Message Sending Failed", Toast.LENGTH_LONG).show()
            }
        })
        i.putExtra(ConversationActivity.MessageFetchListenerIntentKey, object : MessageFetchListener {
            override fun onBeforeMessageFetch(fragment: ConversationFragment) {
            }

            override fun onMessageFetchFailed(fragment: ConversationFragment, error: Error) {
                Toast.makeText(fragment.activity, "Message Loading Failed", Toast.LENGTH_LONG).show()
            }

            override fun onMessageFetchSuccess(fragment: ConversationFragment, messages: List<Message>, isCached: Boolean) {
                Toast.makeText(fragment.activity, "Message Loaded", Toast.LENGTH_LONG).show()
            }
        })
        i.putExtra(ConversationActivity.ConversationFetchListenerIntentKey, object : ConversationFetchListener {
            override fun onBeforeConversationFetch(fragment: ConversationFragment) {
            }

            override fun onConversationFetchFailed(fragment: ConversationFragment, error: Error) {
                Toast.makeText(fragment.activity, "Conversation Loading Failed", Toast.LENGTH_LONG).show()
            }

            override fun onConversationFetchSuccess(fragment: ConversationFragment, conversation: Conversation?) {
                Toast.makeText(fragment.activity, "Conversation Loaded", Toast.LENGTH_LONG).show()
            }

        })
        startActivity(i)
    }

    fun viewMeta(c: Conversation) {
        val f = MetaFragment.newInstance(c)
        f.show(supportFragmentManager, "conversation_meta")
    }

    fun edit(c: Conversation) {
        val f = TitleFragment.newInstance(c.title)
        f.setOnOkBtnClickedListener { t -> updateTitle(c, t) }
        f.show(supportFragmentManager, "update_conversation")
    }

    fun updateTitle(c: Conversation, t: String) {
        mChatContainer.setConversationTitle(c, t, object : SaveCallback<Conversation> {
            override fun onSuccess(new: Conversation?) {
                mAdapter.updateConversation(c, new)
                Toast.makeText(applicationContext, "Title updated.", Toast.LENGTH_SHORT).show()
            }

            override fun onFail(error: Error) {
                showFailureAlert("Fail to delete the conversation: ", error)
            }
        })
    }

    fun confirmLeave(c: Conversation) {
        AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.are_your_sure_to_leave_conversation)
                .setPositiveButton(R.string.yes) { dialog, which -> leave(c) }
                .setNegativeButton(R.string.no, null).show()
    }

    fun leave(c: Conversation) {
        val failAlert = AlertDialog.Builder(this)
                .setTitle("Oops")
                .setNeutralButton(R.string.dismiss, null)
                .create()
        mChatContainer.leaveConversation(c, object : LambdaResponseHandler() {
            override fun onLambdaFail(error: Error?) {
                val alertMessage = "Fail to leave the conversation: ${error?.message}"
                Log.w(LOG_TAG, alertMessage)
                failAlert.setMessage(alertMessage)
                failAlert.show()
            }

            override fun onLambdaSuccess(result: JSONObject?) {
                Log.i(LOG_TAG, "Successfully leave the conversation")
                Toast.makeText(
                        applicationContext,
                        "Successfully leave the conversation",
                        Toast.LENGTH_SHORT
                ).show()
                mAdapter.deleteConversation(c.id)
            }
        })
    }

    fun confirmDelete(c: Conversation) {
        AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.are_your_sure_to_delete_conversation)
                .setPositiveButton(R.string.yes) { dialog, which -> delete(c) }
                .setNegativeButton(R.string.no, null).show()
    }

    fun delete(c: Conversation) {
        mChatContainer.deleteConversation(c, object : DeleteCallback<Boolean> {
            override fun onFail(error: Error) {
                showFailureAlert("Fail to delete the conversation: ", error)
            }

            override fun onSuccess(result: Boolean?) {
                Log.i(LOG_TAG, "Successfully delete the conversation")
                Toast.makeText(applicationContext, "Conversation deleted.", Toast.LENGTH_SHORT).show()
                mAdapter.deleteConversation(c.id)
            }
        })
    }

    fun showFailureAlert(msg: String, error: Error) {
        val failAlert = AlertDialog.Builder(this)
                .setTitle("Oops")
                .setNeutralButton(R.string.dismiss, null)
                .create()
        val alertMessage = msg + error.detailMessage
        Log.w(LOG_TAG, alertMessage)
        failAlert.setMessage(alertMessage)
        failAlert.show()
    }

    fun updateAdmins(c: Conversation) {
        val f = UserIdsFragment.newInstance(getString(R.string.add_remove_admins), c.adminIds)
        f.setConversation(c)
        f.setOnOkBtnClickedListener { ids ->
            c.adminIds?.let { adminIds ->
                Toast.makeText(applicationContext, "Updating admins...", Toast.LENGTH_SHORT).show()

                val idsToRemove = adminIds.toMutableList()
                idsToRemove.removeAll(ids.toList())

                mChatContainer.removeConversationAdmins(
                        c,
                        idsToRemove,
                        object : SaveCallback<Conversation> {
                            override fun onSuccess(updatedConv: Conversation?) {
                                Toast.makeText(
                                        applicationContext,
                                        "Admins updated.",
                                        Toast.LENGTH_SHORT
                                ).show()
                                updatedConv?.id?.let { convId ->
                                    this@ConversationsActivity.refreshConversation(convId)
                                }
                            }

                            override fun onFail(error: Error) {
                                Toast.makeText(
                                        applicationContext,
                                        "Failed to updated admin: ${error.detailMessage}.",
                                        Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                )
            }
        }
        f.show(supportFragmentManager, "update_admins")
    }

    fun updateParticipants(c: Conversation) {
        val f = UserIdsFragment.newInstance(getString(R.string.add_remove_participants), c.participantIds)

        f.setOnOkBtnClickedListener { ids ->
            // distinguish old and new
            Toast.makeText(applicationContext, "Updating participants...", Toast.LENGTH_SHORT).show()
            val idsToRemove = c.participantIds?.toMutableList()
            idsToRemove?.removeAll(ids.toList())

            mChatContainer.removeConversationParticipants(c, idsToRemove!!, object : SaveCallback<Conversation> {
                override fun onSuccess(new: Conversation?) {
                    mChatContainer.addConversationParticipants(c, ids, object : SaveCallback<Conversation> {
                        override fun onSuccess(new: Conversation?) {
                            // the new doesn't have participant ids

                            mChatContainer.getConversation(new?.id!! , object: GetCallback<Conversation> {
                                override fun onSuccess(newWithParticipantIds: Conversation?) {
                                    mAdapter.updateConversation(c, newWithParticipantIds)
                                    Toast.makeText(applicationContext, "Participants updated.", Toast.LENGTH_SHORT).show()
                                }

                                override fun onFail(error: Error) {
                                }
                            })
                        }

                        override fun onFail(error: Error) {
                            Toast.makeText(applicationContext, error.detailMessage, Toast.LENGTH_SHORT).show()
                        }
                    })

                }

                override fun onFail(error: Error) {
                    Toast.makeText(applicationContext, error.detailMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }
        f.show(supportFragmentManager, "update_participants")
    }
}
