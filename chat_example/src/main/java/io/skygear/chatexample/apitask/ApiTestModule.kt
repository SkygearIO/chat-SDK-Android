package io.skygear.chatexample.apitask

import android.app.Activity
import android.content.Context
import android.view.View
import io.skygear.chatexample.ApiTask
import io.skygear.plugins.chat.ChatContainer
import io.skygear.skygear.Container

interface ApiTestModule {
    fun onLoadCustomView(context: Context): View?
    fun onApiTest(activity: Activity, skygear: Container, chatContainer: ChatContainer, task: ApiTask, view: View)
    fun onStop()
}