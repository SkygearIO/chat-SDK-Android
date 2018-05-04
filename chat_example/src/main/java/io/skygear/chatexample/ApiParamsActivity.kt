package io.skygear.chatexample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.google.gson.Gson
import io.skygear.chatexample.apitask.Utils
import io.skygear.plugins.chat.ChatContainer
import io.skygear.plugins.chat.GetParticipantsCallback
import io.skygear.plugins.chat.Participant
import io.skygear.skygear.Container
import io.skygear.skygear.Error
import kotlinx.android.synthetic.main.activity_api_params.*

class ApiParamsActivity : AppCompatActivity() {

    private var paramsAdapter: ApiParamsAdapter? = null
    private var usersAdapter: ChatUsesAdapter? = null
    val mSkygear: Container = Container.defaultContainer(this)
    val mChatContainer: ChatContainer

    init{
        mChatContainer = ChatContainer.getInstance(mSkygear)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_params)
        title = fetchExtras().name

        initViews()
        initParams(fetchExtras())
    }

    private fun initViews() {
        continue_btn.setOnClickListener { submitParams() }
    }

    private fun fetchExtras(): ApiTask {
        return intent.getSerializableExtra(EXTRAS_KEY) as ApiTask
    }

    private fun initParams(apiTask: ApiTask) {
        if(apiTask.loginRequired) {
            // Pick User IDs
            if (!Utils.isLoggedIn(this)) {
                Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        }

        if(apiTask.params.isEmpty()) {
            val intent = Intent(this, ApiTestActivity::class.java)
            intent.putExtra(EXTRAS_KEY, apiTask)
            startActivity(intent)
            finish()
            return
        }

        initRecyclerView(apiTask)
    }

    private fun initRecyclerView(apiTask: ApiTask) {
        recycler_view.layoutManager = LinearLayoutManager(this)

        if(isPickingUserID()) {
            usersAdapter = ChatUsesAdapter(mSkygear.auth.currentUser.id)
            ParticipantsFetcher(mSkygear).fetch(object : GetParticipantsCallback {
                override fun onGetCachedResult(participantsMap: MutableMap<String, Participant>?) {
                    usersAdapter?.setParticipants(participantsMap)
                }

                override fun onSuccess(participantsMap: MutableMap<String, Participant>?) {
                    usersAdapter?.setParticipants(participantsMap)
                }


                override fun onFail(error: Error) {
                }
            })
        } else {
            // Input params
            paramsAdapter = ApiParamsAdapter(this, apiTask)
        }
        recycler_view.adapter = paramsAdapter ?: usersAdapter
    }

    private fun submitParams() {
        if(!isPickingUserID()) {
            val finalAdapter = paramsAdapter
            if (finalAdapter != null) {
                if (!finalAdapter.isAllParamsFilled()) {
                    Toast.makeText(this, R.string.params_incomplete, Toast.LENGTH_SHORT).show()
                    return
                }
                val intent = Intent(this, ApiTestActivity::class.java)
                intent.putExtra(EXTRAS_KEY, finalAdapter.getParams())
                startActivity(intent)
            }
        } else {
            val apiTask = fetchExtras()
            val finalAdapter = usersAdapter
            if (finalAdapter != null) {
                val listOfUsers = finalAdapter.getSelected()
                if(listOfUsers.isEmpty()) {
                    Toast.makeText(this, R.string.params_incomplete, Toast.LENGTH_SHORT).show()
                    return
                }
                val listOfIds = listOfUsers.map { it.id }.toMutableSet()
                if(Utils.isLoggedIn(this)) {
                    listOfIds.add(mSkygear.auth.currentUser.id)
                }
                apiTask.params[USER_ID_KEY] = Gson().toJson(listOfIds)
                val intent = Intent(this, ApiTestActivity::class.java)
                intent.putExtra(EXTRAS_KEY, apiTask)
                startActivity(intent)
            }
        }
    }

    private fun isPickingUserID(): Boolean {
        return fetchExtras().params.containsKey(USER_ID_KEY)
    }

    companion object {
        const val EXTRAS_KEY = "extras"
        const val USER_ID_KEY = "userIDs"
    }
}
