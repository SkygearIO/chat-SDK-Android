package io.skygear.chatexample.apitask

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import io.skygear.chatexample.ApiTask
import io.skygear.chatexample.R
import io.skygear.chatexample.logger.Log
import io.skygear.plugins.chat.*
import io.skygear.skygear.Container
import io.skygear.skygear.Error

/**
 * Created by camerash on 4/29/18.
 * Subscribe conversation message API Test Module
 */
class SubscribeConversationMessage: ApiTestModule {

    var unsubscribeBtn: Button? = null
    var chatContainer: ChatContainer? = null
    var conversation: Conversation? = null

    override fun onLoadCustomView(context: Context): View? {
        // Root layout
        val frame = LinearLayout(context)
        val rootParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        frame.layoutParams = rootParams
        frame.gravity = Gravity.CENTER

        // Unsubscribe button
        unsubscribeBtn = Button(context)
        unsubscribeBtn?.setText(R.string.unsubscribe_btn)
        unsubscribeBtn?.isEnabled = false
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        unsubscribeBtn?.layoutParams = params
        frame.addView(unsubscribeBtn)
        return frame
    }

    override fun onApiTest(activity: Activity, skygear: Container, chatContainer: ChatContainer, task: ApiTask, view: View) {
        val conversationID = task.params["conversationID"]
        if(conversationID == null) {
            Log.i(javaClass.simpleName, "Error: conversationID null")
            return
        }

        Log.i(javaClass.simpleName, "Fetching conversation with ID: \"$conversationID\"...")
        chatContainer.getConversation(conversationID, object: GetCallback<Conversation> {
            override fun onSucc(obj: Conversation?) {
                if(obj == null) {
                    Log.i(javaClass.simpleName, "Fetch conversation failed with reason: Conversation null")
                    return
                }

                Log.i(javaClass.simpleName, "Fetch conversation successful\n")
                Log.i(javaClass.simpleName, "Subscribing to conversation message...")

                var failed = false

                chatContainer.subscribeConversationMessage(obj, object: MessageSubscriptionCallback(obj){
                    override fun notify(eventType: String, message: Message) {
                        Log.i(javaClass.simpleName, "\nMessage received")
                        Log.i(javaClass.simpleName, "Message body: ${message.body}")
                    }

                    override fun onSubscriptionFail(error: Error) {
                        failed = true
                        Log.i(javaClass.simpleName, "Subscribe conversation failed with reason: ${error.detailMessage}")
                    }
                })

                Handler().postDelayed({
                    if(!failed) {
                        Log.i(javaClass.simpleName, "Conversation subscribed, any message received will be printed here")
                        setupUnsubscribeButton(chatContainer, obj)
                        this@SubscribeConversationMessage.chatContainer = chatContainer
                        this@SubscribeConversationMessage.conversation = obj
                    }
                }, 1000)
            }

            override fun onFail(error: Error) {
                Log.i(javaClass.simpleName, "Fetch conversation failed with reason: ${error.detailMessage}")
            }
        })
    }

    override fun onStop() {
        val finalChatContainer = chatContainer
        val finalConversation = conversation
        if(finalChatContainer != null && finalConversation != null) {
            finalChatContainer.unsubscribeConversationMessage(finalConversation)
        }
    }

    private fun setupUnsubscribeButton(chatContainer: ChatContainer, conversation: Conversation) {
        unsubscribeBtn?.isEnabled = true
        unsubscribeBtn?.setOnClickListener {
            Log.i(javaClass.simpleName, "\nUnsubscribing from channel...")
            chatContainer.unsubscribeConversationMessage(conversation)
            Log.i(javaClass.simpleName, "Unsubscribed from channel")
            unsubscribeBtn?.isEnabled = false
        }
    }
}