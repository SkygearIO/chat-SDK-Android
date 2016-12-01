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
import io.skygear.plugins.chat.*
import io.skygear.skygear.Container
import io.skygear.skygear.LambdaResponseHandler
import io.skygear.skygear.LogoutResponseHandler
import org.json.JSONObject
import java.util.*

class ConversationsActivity : AppCompatActivity() {
    private val LOG_TAG: String? = "ConversationsActivity"

    private val mSkygear: Container
    private val mChatContainer: ChatContainer
    private val mAdapter: ConversationsAdapter = ConversationsAdapter()
    private var mConversationsRv: RecyclerView? = null

    init {
        mSkygear = Container.defaultContainer(this)
        mChatContainer = ChatContainer.getInstance(mSkygear)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        mConversationsRv = findViewById(R.id.conversations_rv) as RecyclerView
        mConversationsRv?.adapter = mAdapter
        mConversationsRv?.layoutManager = LinearLayoutManager(this)
        mAdapter.setOnClickListener {
            c -> showOptions(c)
        }
    }

    override fun onResume() {
        super.onResume()
        getAllConversations()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.conversation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.log_out_menu -> {
                confirmLogOut()
                return true
            }
            R.id.add_conversation_menu -> {
                startActivity(Intent(this, CreateConversationActivity::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun getAllConversations() {
        mChatContainer.getConversations(object : GetCallback<List<Conversation>> {
            override fun onSucc(list: List<Conversation>?) {
                mAdapter.setConversations(list)
            }

            override fun onFail(failReason: String?) {

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

        mSkygear.logout(object : LogoutResponseHandler() {
            override fun onLogoutSuccess() {
                loading.dismiss()

                logoutSuccess()
            }

            override fun onLogoutFail(reason: String) {
                loading.dismiss()

                logoutFail()
            }
        })
    }

    fun logoutSuccess() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun logoutFail() {
        AlertDialog.Builder(this).setTitle(R.string.logout_failed).show()
    }

    fun showOptions(c: Conversation) {
        val builder = AlertDialog.Builder(this)
        val items = resources.getStringArray(R.array.conversation_options)
        builder.setItems(items, { d, i -> when(i) {
            0 -> enter(c)
            1 -> edit(c)
            2 -> confirmLeave(c)
            3 -> updateAdmins(c)
            4 -> updateParticipants(c)
        } })
        val alert = builder.create()
        alert.show()
    }

    fun enter(c: Conversation) {
        startActivity(ConversationActivity.newIntent(c, this))
    }

    fun edit(c: Conversation) {
        val f = TitleFragment.newInstance(c.title)
        f.setOnOkBtnClickedListener { t -> updateTitle(c, t) }
        f.show(supportFragmentManager, "update_conversation")
    }

    fun updateTitle(c: Conversation, t: String) {
        mChatContainer.setConversationTitle(c, t, object : SaveCallback<Conversation> {
            override fun onSucc(new: Conversation?) {
                mAdapter.updateConversation(c, new)
            }

            override fun onFail(failReason: String?) {

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
            override fun onLambdaFail(reason: String?) {
                val alertMessage = "Fail to leave the conversation: $reason"
                Log.w(LOG_TAG, alertMessage)
                failAlert.setMessage(alertMessage)
                failAlert.show()
            }

            override fun onLambdaSuccess(result: JSONObject?) {
                Log.i(LOG_TAG, "Successfully leave the conversation")
                getAllConversations()
            }
        } )
    }

    fun updateAdmins(c: Conversation) {
        val f = UserIdsFragment.newInstance(getString(R.string.add_remove_admins), c.adminIds)
        f.setOnOkBtnClickedListener { ids ->
            mChatContainer.setConversationAdminIds(c, HashSet(ids), object : SaveCallback<Conversation> {
                override fun onSucc(new: Conversation?) {
                    mAdapter.updateConversation(c, new)
                }

                override fun onFail(failReason: String?) {

                }
            })
        }
        f.show(supportFragmentManager, "update_admins")
    }

    fun updateParticipants(c: Conversation) {
        val f = UserIdsFragment.newInstance(getString(R.string.add_remove_participants), c.participantIds)
        f.setOnOkBtnClickedListener { ids ->
            mChatContainer.setConversationParticipants(c, HashSet(ids), object : SaveCallback<Conversation> {
                override fun onSucc(new: Conversation?) {
                    mAdapter.updateConversation(c, new)
                }

                override fun onFail(failReason: String?) {

                }
            })
        }
        f.show(supportFragmentManager, "update_participants")
    }
}
