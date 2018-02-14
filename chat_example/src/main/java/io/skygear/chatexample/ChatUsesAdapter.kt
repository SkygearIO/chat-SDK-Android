package io.skygear.chatexample

import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.skygear.plugins.chat.Participant

class ChatUsesAdapter(currentUserId: String?) : RecyclerView.Adapter<ChatUsesAdapter.ViewHolder>() {
    private val LOG_TAG: String? = "ChatUsesAdapter"

    private var mParticipants: List<Participant> = listOf()
    private var mSelectedParticipants: MutableList<Participant> = mutableListOf()
    private val mCurrentUserId = currentUserId

    fun setParticipants(participants: Map<String, Participant>?) {
        if (participants != null) {
            mParticipants = participants.values.filter {
                it.id != mCurrentUserId
            }

            notifyDataSetChanged()
        }
    }

    fun getSelected(): List<Participant> {
        return mSelectedParticipants
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_user_id, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participant: Participant = mParticipants[position]

        holder.idTv.text = participant.id
        if (!participant.record.get("username")?.toString().isNullOrEmpty()) {
                holder.idTv.text = participant.record.get("username").toString()
        }
        holder.idCb.isChecked = participant in mSelectedParticipants

        holder.idCb.setOnClickListener { it: View? ->
            val cb = (it as AppCompatCheckBox)

            if(cb.isChecked) {
                mSelectedParticipants.add(participant)
            } else {
                mSelectedParticipants.remove(participant)
            }
        }
    }

    override fun getItemCount(): Int {
        return mParticipants.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idCb = view.findViewById<AppCompatCheckBox>(R.id.user_id_cb)
        var idTv = view.findViewById<TextView>(R.id.user_id_tv)
    }
}
