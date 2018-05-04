package io.skygear.chatexample.apitask

import android.app.Activity
import android.content.Context
import android.view.View
import io.skygear.chatexample.ApiTask
import io.skygear.chatexample.logger.Log
import io.skygear.plugins.chat.*
import io.skygear.skygear.Container
import io.skygear.skygear.Error

class SendMessage: ApiTestModule {
    
    override fun onLoadCustomView(context: Context): View? {
        return null
    }

    override fun onApiTest(activity: Activity, skygear: Container, chatContainer: ChatContainer, task: ApiTask, view: View) {
        val conversationID = task.params["conversationID"]
        val message = task.params["message"]
        if(conversationID == null) {
            Log.i(javaClass.simpleName, "Error: conversationID null")
            return
        }
        if(message == null) {
            Log.i(javaClass.simpleName, "Error: message null")
            return
        }

        Log.i(javaClass.simpleName, "Fetching conversation with ID: \"$conversationID\"...")
        chatContainer.getConversation(conversationID, object: GetCallback<Conversation> {
            override fun onSuccess(obj: Conversation?) {
                if(obj == null) {
                    Log.i(javaClass.simpleName, "Fetch conversation failed with reason: Conversation null")
                    return
                }

                Log.i(javaClass.simpleName, "Fetch conversation successful\n")
                Log.i(javaClass.simpleName, "Sending message: \"$message\"...")

                chatContainer.sendMessage(obj, message, null, null, object: SaveCallback<Message>{
                    override fun onSuccess(msg: Message?) {
                        if(msg == null) {
                            Log.i(javaClass.simpleName, "Send message failed with reason: Message null")
                            return
                        }

                        Log.i(javaClass.simpleName, "Message sent, message ID: ${msg.id}")
                        Log.i(javaClass.simpleName, "Message ID copied to clipboard")
                        Utils.copyToClipboard(activity, javaClass.simpleName, msg.id)
                    }

                    override fun onFail(error: Error) {
                        Log.i(javaClass.simpleName, "Send message failed with reason: ${error.detailMessage}")
                    }
                })
            }

            override fun onFail(error: Error) {
                Log.i(javaClass.simpleName, "Fetch conversation failed with reason: ${error.detailMessage}")
            }
        })
    }

    override fun onStop() {}
}