package io.skygear.chatexample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import io.skygear.plugins.chat.message.Message

class ConversationAdapter : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {
    private val LOG_TAG = "Adapter"

    private var mMessages: MutableList<Message> = mutableListOf()

    fun setMessages(messages: List<Message>?) {
        if (messages != null) {
            mMessages = messages.toMutableList()
        } else {
            mMessages = mutableListOf();
        }

        notifyDataSetChanged()
    }

    fun addMessage(message: Message?) {
        if (message != null) {
            mMessages.add(0, message)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_message, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message: Message? = mMessages[position]

        if (!message?.body.isNullOrEmpty()) {
            holder.bodyTv.visibility = View.VISIBLE
            holder.bodyTv.text = message?.body
        } else {
            holder.bodyTv.visibility = View.GONE
        }

        if (message?.asset != null) {
            holder.assetIv.visibility = View.VISIBLE
            Picasso.with(holder.assetIv.context).load(message?.asset?.url).into(holder.assetIv);
        } else {
            holder.assetIv.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mMessages.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)  {
        val bodyTv = view.findViewById(R.id.message_body_tv) as TextView
        val assetIv = view.findViewById(R.id.message_asset_iv) as ImageView
    }
}
