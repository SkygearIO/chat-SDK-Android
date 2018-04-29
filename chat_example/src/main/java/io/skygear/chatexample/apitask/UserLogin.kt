package io.skygear.chatexample.apitask

import android.app.Activity
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

    override fun onLoadCustomView(context: Context): View? {
        return null
    }

    override fun onApiTest(activity: Activity, skygear: Container, chatContainer: ChatContainer, task: ApiTask, view: View) {
        val username = task.params["username"]
        val password = task.params["password"]
        Log.i(javaClass.simpleName, "Logging in with Username: \"$username\" and Password: \"$password\"...\n")

        skygear.auth.loginWithUsername(username, password, object: AuthResponseHandler() {
            override fun onAuthSuccess(user: Record) {
                Log.i(javaClass.simpleName, "Login successful, User ID: ${user.id}")
                Log.i(javaClass.simpleName, "User ID copied to clipboard")
                Utils.copyToClipboard(activity, javaClass.simpleName, user.id)
            }

            override fun onAuthFail(error: Error) {
                Log.i(javaClass.simpleName, "Login failed with reason: ${error.detailMessage}")
            }
        })
    }
}