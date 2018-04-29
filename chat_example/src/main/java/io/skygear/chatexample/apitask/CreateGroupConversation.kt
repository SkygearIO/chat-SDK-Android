package io.skygear.chatexample.apitask

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.skygear.chatexample.ApiTask
import io.skygear.chatexample.logger.Log
import io.skygear.plugins.chat.ChatContainer
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.SaveCallback
import io.skygear.skygear.Container
import io.skygear.skygear.Error
import java.text.SimpleDateFormat
import java.util.*

@Suppress("UNCHECKED_CAST")
/**
 * Created by camerash on 4/29/18.
 * Create group conversation API Test Module
 */
class CreateGroupConversation: ApiTestModule {

    private val TAG = javaClass.simpleName

    override fun onLoadCustomView(context: Context): View? {
        return null
    }

    override fun onApiTest(activity: Activity, skygear: Container, chatContainer: ChatContainer, task: ApiTask, view: View) {
        if (!Utils.isLoggedIn(activity)) {
            Toast.makeText(activity, "Please login first!", Toast.LENGTH_SHORT).show()
            activity.finish()
            return
        }

        val userIDs = Gson().fromJson<MutableSet<String>>(task.params["userIDs"])
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
        Log.i(TAG, "Creating conversation...")

        chatContainer.createConversation(userIDs, "$timestamp Test Conversation", null, null, object: SaveCallback<Conversation> {
            override fun onSucc(obj: Conversation?) {
                if(obj == null) {
                    Log.i(TAG, "Create conversation failed with reason: Conversation null")
                    return
                }
                Log.i(TAG, "Create conversation successful, conversation ID: ${obj.id}")
                Log.i(TAG, "Conversation ID copied to clipboard")
                Utils.copyToClipboard(activity, TAG, obj.id)
            }

            override fun onFail(error: Error) {
                Log.i(TAG, "Create conversation failed with reason: ${error.detailMessage}")
            }
        })
    }

    private inline fun <reified T> Gson.fromJson(json: String?) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)
}