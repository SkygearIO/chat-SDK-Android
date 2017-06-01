package io.skygear.chatexample

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import io.skygear.plugins.chat.Message

class ConversationAdapter : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {
    private val LOG_TAG = "Adapter"

    private var mMessages: MutableList<Message> = mutableListOf()
    private var mListener: (Message) -> Unit = {}

    fun setOnClickListener(listener: (Message) -> Unit) {
        mListener = listener
    }

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

    fun updateMessage(message: Message) {
        val idx = mMessages.indexOfFirst { it.id == message.id }
        if (idx != -1) {
            mMessages[idx] = message
            notifyDataSetChanged()
        }
    }

    fun deleteMessage(message:Message) {
        val idx = mMessages.indexOfFirst { it.id == message.id }
        if (idx != - 1)
        {
            mMessages.removeAt(idx)
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
            Picasso.with(holder.assetIv.context).load(message?.asset?.url).into(holder.assetIv)
        } else {
            holder.assetIv.visibility = View.GONE
        }

        when (message?.status) {
            Message.Status.ALL_READ -> {
                holder.statusIv.setImageResource(R.drawable.ic_blue_tick)
            }
            Message.Status.SOME_READ -> {
                holder.statusIv.setImageResource(R.drawable.ic_green_tick)
            }
            else -> {
                holder.statusIv.setImageResource(android.R.color.transparent)
            }
        }

        with (holder.itemView)
        {
            tag = message
            setOnClickListener { v ->
                val m = v.tag as Message
                mListener(m)
            }
        }
    }

    override fun getItemCount(): Int {
        return mMessages.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)  {
        val bodyTv = view.findViewById(R.id.message_body_tv) as TextView
        val statusIv = view.findViewById(R.id.message_status_iv) as ImageView
        val assetIv = view.findViewById(R.id.message_asset_iv) as ImageView
    }
}
