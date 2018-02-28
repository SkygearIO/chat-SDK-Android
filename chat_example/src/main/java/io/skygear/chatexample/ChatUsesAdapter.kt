package io.skygear.chatexample

import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.skygear.plugins.chat.ChatUser

class ChatUsesAdapter(currentUserId: String?) : RecyclerView.Adapter<ChatUsesAdapter.ViewHolder>() {
    private val LOG_TAG: String? = "ChatUsesAdapter"

    private var mChatUsers: List<ChatUser> = listOf()
    private var mSelectedChatUsers: MutableList<ChatUser> = mutableListOf()
    private val mCurrentUserId = currentUserId

    fun setChatUsers(chatUsers: Map<String, ChatUser>?) {
        if (chatUsers != null) {
            mChatUsers = chatUsers.values.filter {
                it.id != mCurrentUserId
            }

            notifyDataSetChanged()
        }
    }

    fun getSelected(): List<ChatUser> {
        return mSelectedChatUsers
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_user_id, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatUser: ChatUser = mChatUsers[position]

        holder.idTv.text = chatUser.id
        if (!chatUser.record.get("username")?.toString().isNullOrEmpty()) {
                holder.idTv.text = chatUser.record.get("username").toString()
        }
        holder.idCb.isChecked = chatUser in mSelectedChatUsers

        holder.idCb.setOnClickListener { it: View? ->
            val cb = (it as AppCompatCheckBox)

            if(cb.isChecked) {
                mSelectedChatUsers.add(chatUser)
            } else {
                mSelectedChatUsers.remove(chatUser)
            }
        }
    }

    override fun getItemCount(): Int {
        return mChatUsers.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idCb = view.findViewById<AppCompatCheckBox>(R.id.user_id_cb)
        var idTv = view.findViewById<TextView>(R.id.user_id_tv)
    }
}
