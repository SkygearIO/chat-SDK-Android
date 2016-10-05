package io.skygear.chatexample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.skygear.plugins.chat.Message

class ConversationAdapter : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {
    private val LOG_TAG = "Adapter"

    private var mMessages: List<Message> = listOf()

    fun setMessages(messages: List<Message>?) {
        if (messages != null) {
            mMessages = messages
        } else {
            mMessages = listOf();
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_message, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message: Message? = mMessages[position]

        holder.bodyTv.text = message?.body
    }

    override fun getItemCount(): Int {
        return mMessages.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)  {
        val bodyTv = view.findViewById(R.id.message_body_tv) as TextView
    }
}
