package io.skygear.chatexample

import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.skygear.plugins.chat.Participant

class UserIdsAdapter(val currentUserId: String?) : RecyclerView.Adapter<UserIdsAdapter.ViewHolder>() {
    private val LOG_TAG: String? = "UserIdsAdapter"

    private var participants: List<Participant> = listOf()
    private var selectedIds: MutableList<String> = mutableListOf()

    fun setUserIds(participants: List<Participant>?, selectedIds: List<String>?) {
        if (participants != null) {
            this.participants = participants.filter {
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
        val participant: Participant = participants[position]
        holder.idTv.text = participant.record.get("username").toString()

        holder.idCb.isChecked = participant.id in selectedIds

        holder.idCb.setOnClickListener { it: View? ->
            val cb = (it as AppCompatCheckBox)

            if(cb.isChecked) {
                selectedIds.add(participant.id)
            } else {
                selectedIds.remove(participant.id)
            }
        }
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idCb = view.findViewById<AppCompatCheckBox>(R.id.user_id_cb)
        var idTv = view.findViewById<TextView>(R.id.user_id_tv)
    }
}
