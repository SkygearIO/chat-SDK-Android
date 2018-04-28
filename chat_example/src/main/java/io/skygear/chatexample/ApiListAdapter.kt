package io.skygear.chatexample

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by camerash on 4/28/18.
 * Recycler adapter for Api Test List
 */
class ApiListAdapter (var context: Context,
                      private val apiList: ArrayList<ApiTask>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ApiTaskClickListener {
        fun onApiTaskClicked(task: ApiTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_api_task, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder
        val task = apiList[position]

        vh.taskName.text = task.name

        val ctx = context
        if(ctx is ApiTaskClickListener) {
            vh.taskName.setOnClickListener { ctx.onApiTaskClicked(task) }
        }
    }

    override fun getItemCount(): Int = apiList.size

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var taskName: TextView = itemView.findViewById(R.id.taskName)

    }
}