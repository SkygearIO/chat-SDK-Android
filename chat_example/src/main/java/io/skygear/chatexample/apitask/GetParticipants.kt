package io.skygear.chatexample.apitask

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import io.skygear.chatexample.ApiTask
import io.skygear.chatexample.logger.Log
import io.skygear.plugins.chat.ChatContainer
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.GetCallback
import io.skygear.skygear.Container
import io.skygear.skygear.Error

/**
 * Created by camerash on 4/29/18.
 * Get participants API Test Module
 */
class GetParticipants: ApiTestModule {

    var listView: ListView? = null

    override fun onLoadCustomView(context: Context): View? {
        listView = ListView(context)
        return listView
    }

    override fun onApiTest(activity: Activity, skygear: Container, chatContainer: ChatContainer, task: ApiTask, view: View) {
        val conversationID = task.params["conversationID"]
        if(conversationID == null) {
            Log.i(javaClass.simpleName, "Error: conversationID null")
            return
        }

        Log.i(javaClass.simpleName, "Fetching conversation with ID: \"$conversationID\"...\n")
        chatContainer.getConversation(conversationID, object: GetCallback<Conversation> {
            override fun onSucc(obj: Conversation?) {
                if(obj == null) {
                    Log.i(javaClass.simpleName, "Fetch conversation failed with reason: Conversation null")
                    return
                }
                val idSet = obj.participantIds
                if(idSet == null) {
                    Log.i(javaClass.simpleName, "Get participants failed with reason: Participants null")
                    return
                }
                val idList = idSet.map { it }.toMutableList()
                val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, idList)
                listView?.adapter = adapter
                listView?.setOnItemLongClickListener { _, _, i, _ ->
                    Utils.copyToClipboard(activity, javaClass.simpleName, idList[i])
                    Toast.makeText(activity, "Participant ID copied", Toast.LENGTH_SHORT).show()
                    true
                }

                Log.i(javaClass.simpleName, "Fetch conversation successful")
                Log.i(javaClass.simpleName, "Long click the items above to copy the respective participant ID")
            }

            override fun onFail(error: Error) {
                Log.i(javaClass.simpleName, "Fetch conversations failed with reason: ${error.detailMessage}")
            }
        })
    }

    override fun onStop() {}
}