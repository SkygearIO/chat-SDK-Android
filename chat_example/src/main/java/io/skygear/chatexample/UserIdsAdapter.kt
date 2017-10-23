package io.skygear.chatexample

import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.skygear.plugins.chat.ChatUser

class UserIdsAdapter(val currentUserId: String?) : RecyclerView.Adapter<UserIdsAdapter.ViewHolder>() {
    private val LOG_TAG: String? = "UserIdsAdapter"

    private var chatUsers: List<ChatUser> = listOf()
    private var selectedIds: MutableList<String> = mutableListOf()

    fun setUserIds(chatUsers: List<ChatUser>?, selectedIds: List<String>?) {
        if (chatUsers != null) {
            this.chatUsers = chatUsers.filter {
                it.id != currentUserId
            }
        }

        if (selectedIds != null) {
            this.selectedIds = selectedIds.toMutableList()
        }

        notifyDataSetChanged()
    }

    fun getSelectedIds(): List<String> {
        return selectedIds
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_user_id, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatUser: ChatUser = chatUsers[position]

        holder.idTv.text = chatUser.id
        holder.idCb.isChecked = chatUser.id in selectedIds
        holder.idCb.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                selectedIds.add(chatUser.id)
            } else {
                selectedIds.remove(chatUser.id)
            }
        }
    }

    override fun getItemCount(): Int {
        return chatUsers.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idCb = view.findViewById<AppCompatCheckBox>(R.id.user_id_cb)
        var idTv = view.findViewById<TextView>(R.id.user_id_tv)
    }
}
