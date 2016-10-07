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
    private var mListener: (Conversation) -> Unit = {}

    fun setOnClickListener(listener: (Conversation) -> Unit) {
        mListener = listener
    }

    fun setConversations(conversations: List<Conversation>?) {
        if (conversations != null) {
            mConversations = conversations
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_conversation, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation: Conversation? = mConversations[position]

        holder.nameTv.text = conversation?.title
        holder.idTv.text = conversation?.id

        with(holder.container) {
            tag = conversation
            setOnClickListener { v ->
                val c = v.tag as Conversation
                mListener(c)
            }
        }
    }

    override fun getItemCount(): Int {
        return mConversations.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)  {
        val nameTv = view.findViewById(R.id.conversation_name_tv) as TextView
        val idTv = view.findViewById(R.id.conversation_id_tv) as TextView
        val container: View = view.findViewById(R.id.conversation_item_view)
    }
}
