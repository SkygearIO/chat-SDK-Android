package io.skygear.chatexample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.UserConversation

class ConversationsAdapter : RecyclerView.Adapter<ConversationsAdapter.ViewHolder>() {
    private val LOG_TAG = "Adapter"

    private var mConversations: List<UserConversation> = listOf()
    private var mListener: (UserConversation) -> Unit = {}

    fun setOnClickListener(listener: (UserConversation) -> Unit) {
        mListener = listener
    }

    fun setConversations(conversations: List<UserConversation>?) {
        mConversations = conversations ?: listOf()
        notifyDataSetChanged()
    }

    fun updateConversation(old: UserConversation, new: Conversation?) {
        if (new != null) {
            val conversations: MutableList<UserConversation> = mConversations.toMutableList()
            val idx = conversations.indexOf(old)

            if (idx != -1) {
                conversations[idx].conversation = new
                notifyDataSetChanged()
            }
        }
    }

    fun deleteConversation(id: String?) {
        if (id != null) {
            val conversations: MutableList<UserConversation> = mConversations.toMutableList()
            val conversation = conversations.find { it.id.equals(id) }
            conversations.remove(conversation)
            mConversations = conversations.toList()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_conversation, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userConversation = mConversations[position]
        val conversation = userConversation.getConversation()

        holder.nameTv.text = conversation?.title
        holder.idTv.text = conversation?.id
        holder.lastMsgTv.text = userConversation?.lastMessage?.body

        with(holder.container) {
            tag = userConversation
            setOnClickListener { v ->
                val uc = v.tag as UserConversation
                mListener(uc)
            }
        }
    }

    override fun getItemCount(): Int {
        return mConversations.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)  {
        val nameTv = view.findViewById(R.id.conversation_name_tv) as TextView
        val idTv = view.findViewById(R.id.conversation_id_tv) as TextView
        val lastMsgTv = view.findViewById(R.id.conversation_last_msg_tv) as TextView
        val container: View = view.findViewById(R.id.conversation_item_view)
    }
}
