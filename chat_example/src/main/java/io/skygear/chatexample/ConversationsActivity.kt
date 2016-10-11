package io.skygear.chatexample

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import io.skygear.plugins.chat.callbacks.DeleteOneCallback
import io.skygear.plugins.chat.callbacks.GetCallback
import io.skygear.plugins.chat.callbacks.SaveCallback
import io.skygear.plugins.chat.conversation.Conversation
import io.skygear.plugins.chat.conversation.ConversationContainer
import io.skygear.skygear.Container
import io.skygear.skygear.LogoutResponseHandler

class ConversationsActivity : AppCompatActivity() {
    private val LOG_TAG: String? = "ConversationsActivity"

    private var mSkygear: Container? = null
    private var mConversationContainer: ConversationContainer? = null
    private val mAdapter: ConversationsAdapter = ConversationsAdapter()
    private var mConversationsRv: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        mSkygear = Container.defaultContainer(this)
        mConversationContainer = ConversationContainer.getInstance(mSkygear)

        mConversationsRv = findViewById(R.id.conversations_rv) as RecyclerView
        mConversationsRv?.adapter = mAdapter
        mConversationsRv?.layoutManager = LinearLayoutManager(this)
        mAdapter.setOnClickListener {
            c -> showOptions(c)
        }
    }

    override fun onResume() {
        super.onResume()

        mConversationContainer?.getAll(object : GetCallback<List<Conversation>>{
            override fun onSucc(list: List<Conversation>?) {
                mAdapter.setConversations(list)
            }

            override fun onFail(failReason: String?) {

            }
        } )
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

        mSkygear?.logout(object : LogoutResponseHandler() {
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
            2 -> confirmDelete(c)
            3 -> addRmAdmins(c)
            4 -> addRmParticipants(c)
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
        mConversationContainer?.update(c.id, t, object : SaveCallback<Conversation> {
            override fun onSucc(new: Conversation?) {
                mAdapter.updateConversation(c, new)
            }

            override fun onFail(failReason: String?) {

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
        mConversationContainer?.delete(c.id, object : DeleteOneCallback{
            override fun onSucc(deletedId: String?) {

            }

            override fun onFail(failReason: String?) {

            }
        } )
    }

    fun addRmAdmins(c: Conversation) {
        val f = UserIdsFragment.newInstance(getString(R.string.add_remove_admins), c.adminIds)
        f.setOnOkBtnClickedListener { ids ->
            mConversationContainer?.setAdminIds(c.id, ids, object : SaveCallback<Conversation> {
                override fun onSucc(new: Conversation?) {
                    mAdapter.updateConversation(c, new)
                }

                override fun onFail(failReason: String?) {

                }
            })
        }
        f.show(supportFragmentManager, "update_admins")
    }

    fun addRmParticipants(c: Conversation) {
        val f = UserIdsFragment.newInstance(getString(R.string.add_remove_participants), c.participantIds)
        f.setOnOkBtnClickedListener { ids ->
            mConversationContainer?.setParticipantsIds(c.id, ids,object : SaveCallback<Conversation> {
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
