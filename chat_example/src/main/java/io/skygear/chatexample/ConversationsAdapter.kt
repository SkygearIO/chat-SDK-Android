package io.skygear.chatexample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.skygear.plugins.chat.Conversation

class ConversationsAdapter : RecyclerView.Adapter<ConversationsAdapter.ViewHolder>() {
    private val LOG_TAG = "Adapter"

    private var mConversations: List<Conversation> = listOf()
    private var mListener: (Int) -> Unit = {}

    fun setOnClickListener(listener: (Int) -> Unit) {
        mListener = listener
    }

    fun setConversations(conversations: List<Conversation>?) {
        mConversations = conversations ?: listOf()
        notifyDataSetChanged()
    }

    fun getConversation(pos: Int) : Conversation {
        return mConversations[pos]
    }

    fun updateConversation(old: Conversation, new: Conversation?) {
        new?.let { newConv ->
            val conversations: MutableList<Conversation> = mConversations.toMutableList()
            val idx = conversations.indexOf(old)
            if (idx != -1) {
                conversations[idx] = newConv
                mConversations = conversations.toList()
                notifyDataSetChanged()
            }
        }
    }

    fun updateConversation(new: Conversation?) {
        new?.let { newConv ->
            val conversations: MutableList<Conversation> = mConversations.toMutableList()
            val idx = conversations.indexOfFirst { it.id == newConv.id }
            if (idx != -1) {
                conversations[idx] = newConv
                mConversations = conversations.toList()
                notifyDataSetChanged()
            }
        }
    }

    fun addConversation(new: Conversation?) {
        new?.let { addConversations(listOf(it)) }
    }

    fun addConversations(new: List<Conversation>?) {
        val idSet = mConversations.map { it.id }.toSet()
        new?.filter { !idSet.contains(it.id) }?.let { filteredNewConvs ->
            val mutableList = mConversations.toMutableList()
            mutableList.addAll(filteredNewConvs)
            mConversations = mutableList.toList()

            notifyDataSetChanged()
        }
    }

    fun deleteConversation(id: String?) {
        id?.let { idToDelete ->
            mConversations = mConversations.filter { it.id != idToDelete }
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_conversation, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = mConversations[position]

        holder.nameTv.text = conversation?.title
        holder.idTv.text = conversation?.id
        holder.lastMsgTv.text = conversation?.lastReadMessage?.body

        with(holder.container) {
            setOnClickListener { v ->
                mListener(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return mConversations.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTv = view.findViewById<TextView>(R.id.conversation_name_tv)
        val idTv = view.findViewById<TextView>(R.id.conversation_id_tv)
        val lastMsgTv = view.findViewById<TextView>(R.id.conversation_last_msg_tv)
        val container: View = view.findViewById(R.id.conversation_item_view)
    }
}
