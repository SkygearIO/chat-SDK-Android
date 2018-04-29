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
 * Fetch user conversations API Test Module
 */
class FetchUserConversations: ApiTestModule {

    var listView: ListView? = null

    override fun onLoadCustomView(context: Context): View? {
        listView = ListView(context)
        return listView
    }

    override fun onApiTest(activity: Activity, skygear: Container, chatContainer: ChatContainer, task: ApiTask, view: View) {

        Log.i(javaClass.simpleName, "Fetching conversations...\n")
        chatContainer.getConversations(object: GetCallback<List<Conversation>>{
            override fun onSuccess(obj: List<Conversation>?) {
                if(obj == null) {
                    Log.i(javaClass.simpleName, "Fetch conversations failed with reason: Conversation null")
                    return
                }

                val idList = obj.map { it.id }.toMutableList()
                val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, idList)
                listView?.adapter = adapter
                listView?.setOnItemLongClickListener { _, _, i, _ ->
                    Utils.copyToClipboard(activity, javaClass.simpleName, idList[i])
                    Toast.makeText(activity, "Conversation ID copied", Toast.LENGTH_SHORT).show()
                    true
                }

                Log.i(javaClass.simpleName, "Fetch conversation successful")
                Log.i(javaClass.simpleName, "Long click the items above to copy the respective conversation ID")
            }

            override fun onFail(error: Error) {
                Log.i(javaClass.simpleName, "Fetch conversations failed with reason: ${error.detailMessage}")
            }
        })
    }

    override fun onStop() {}
}