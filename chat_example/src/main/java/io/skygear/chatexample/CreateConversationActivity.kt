package io.skygear.chatexample

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.Toast
import io.skygear.plugins.chat.* // ktlint-disable no-wildcard-imports
import io.skygear.skygear.Container
import io.skygear.skygear.Error
import java.util.* // ktlint-disable no-wildcard-imports

class CreateConversationActivity : AppCompatActivity() {
    private val LOG_TAG: String? = "CreateConversation"

    private val mSkygear: Container
    private val mChatContainer: ChatContainer
    private val mAdapter: ChatUsesAdapter
    private var mCreateBtn: Button? = null
    private var mUserIdsRv: RecyclerView? = null

    init {
        mSkygear = Container.defaultContainer(this)
        mChatContainer = ChatContainer.getInstance(mSkygear)
        mAdapter = ChatUsesAdapter(mSkygear.auth.currentUser.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_conversation)

        mUserIdsRv = findViewById<RecyclerView>(R.id.chat_users_rv)
        mCreateBtn = findViewById<Button>(R.id.create_conversation_btn)
        mUserIdsRv?.adapter = mAdapter
        mUserIdsRv?.layoutManager = LinearLayoutManager(this)

        mCreateBtn?.setOnClickListener {
            val selectedUsers = mAdapter.getSelected()
            if (selectedUsers.isNotEmpty()) {
                var usernames = selectedUsers.map{user -> user.record.get("username")}.joinToString(separator = ", ")
                // add myself
                usernames += " and "
                usernames += mSkygear.auth.currentUser.get("username")

                val defaultTitle = String.format("💬 %s", usernames)
                createTitleDialog(defaultTitle)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mChatContainer.getChatUsers(object : GetCallback<List<ChatUser>> {
            override fun onSuccess(list: List<ChatUser>?) {
                mAdapter.setChatUsers(list)
            }

            override fun onFail(error: Error) {
            }
        })
    }

    fun createTitleDialog(defaultTitle: String) {
        val f = TitleFragment()
        val mArgs = Bundle()
        mArgs.putString("text_key", defaultTitle)
        f.setArguments(mArgs)
        f.setOnOkBtnClickedListener { t -> createConversation(mAdapter.getSelected(), t) }
        f.show(supportFragmentManager, "create_conversation")
    }

    fun createConversation(users: List<ChatUser>?, title: String?) {
        if (users != null && users.isNotEmpty()) {
            val participantIds = users.map { it.id }.toMutableSet()
            val currentUser = mSkygear.auth.currentUser
            if (currentUser != null) {
                participantIds.add(currentUser.id)
            }

            val loading = ProgressDialog(this)
            loading.setTitle(R.string.loading)
            loading.setMessage(getString(R.string.creating))
            loading.show()

            mChatContainer.createConversation(participantIds, title, null, null, object : SaveCallback<Conversation> {
                override fun onSuccess(con: Conversation?) {
                    loading.dismiss()
                    toast("Conversation created!")
                    finish()
                }

                override fun onFail(error: Error) {
                    loading.dismiss()
                    if (error.message != null) {
                        toast(error.message.toString())
                    }
                }
            })
        }
    }

    fun toast(r: String) {
        val context = applicationContext
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(context, r, duration)
        toast.show()
    }
}
