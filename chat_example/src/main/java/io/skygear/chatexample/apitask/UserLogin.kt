package io.skygear.chatexample.apitask

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import io.skygear.chatexample.ApiTask
import io.skygear.chatexample.logger.Log
import io.skygear.plugins.chat.ChatContainer
import io.skygear.skygear.AuthResponseHandler
import io.skygear.skygear.Container
import io.skygear.skygear.Error
import io.skygear.skygear.Record


/**
 * Created by camerash on 4/28/18.
 * User login API Test Module
 */
class UserLogin: ApiTestModule {

    private val TAG = javaClass.simpleName

    override fun onLoadCustomView(context: Context): View? {
        return null
    }

    override fun onApiTest(context: Context, skygear: Container, chatContainer: ChatContainer, task: ApiTask, view: View) {
        val username = task.params["username"]
        val password = task.params["password"]
        Log.i(TAG, "Logging in with Username: \"$username\" and Password: \"$password\"...")
        skygear.auth.loginWithUsername(username, password, object: AuthResponseHandler() {
            override fun onAuthSuccess(user: Record) {
                Log.i(TAG, "Login successful, User ID copied to clipboard")
                copyToClipboard(context, user.id)
            }

            override fun onAuthFail(error: Error) {
                Log.i(TAG, "Login failed with reason: ${error.detailMessage}")
            }
        })
    }

    fun copyToClipboard(context:Context, string: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(TAG, string)
        clipboard.primaryClip = clip
    }
}