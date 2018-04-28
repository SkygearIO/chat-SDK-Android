package io.skygear.chatexample.apitask

import android.content.Context
import android.view.View
import io.skygear.chatexample.ApiTask
import io.skygear.plugins.chat.ChatContainer
import io.skygear.skygear.Container

/**
 * Created by camerash on 4/28/18.
 * Base interface for API Testing
 */
interface ApiTestModule {
    fun onLoadCustomView(context: Context): View?
    fun onApiTest(context: Context, skygear: Container, chatContainer: ChatContainer, task: ApiTask, view: View)
}