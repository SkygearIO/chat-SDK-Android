package io.skygear.chatexample.apitask

import android.app.Activity
import android.content.Context
import android.view.View
import io.skygear.chatexample.ApiTask
import io.skygear.chatexample.logger.Log
import io.skygear.plugins.chat.ChatContainer
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.GetCallback
import io.skygear.plugins.chat.SaveCallback
import io.skygear.skygear.Container
import io.skygear.skygear.Error

/**
 * Created by camerash on 4/29/18.
 * Add participant API Test Module
 */
class AddParticipant: ApiTestModule {

    override fun onLoadCustomView(context: Context): View? {
        return null
    }

    override fun onApiTest(activity: Activity, skygear: Container, chatContainer: ChatContainer, task: ApiTask, view: View) {
        val conversationID = task.params["conversationID"]
        val participantID = task.params["participantID"]
        if (conversationID == null) {
            Log.i(javaClass.simpleName, "Error: conversationID null")
            return
        }
        if (participantID == null) {
            Log.i(javaClass.simpleName, "Error: participantID null")
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
                Log.i(javaClass.simpleName, "Adding participant: \"$participantID\"...")

                chatContainer.addConversationParticipant(obj, participantID, object: SaveCallback<Conversation> {
                    override fun onSuccess(obj: Conversation?) {
                        Log.i(javaClass.simpleName, "Add participant successful")
                    }

                    override fun onFail(error: Error) {
                        Log.i(javaClass.simpleName, "Add participant failed with reason: ${error.detailMessage}")
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