package io.skygear.chatexample

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

class ApiParamsAdapter(var context: Context,
                       private val apiTask: ApiTask): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val keyList = ArrayList(apiTask.params.keys)
    val valueList = ArrayList(apiTask.params.values)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_api_params, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder

        vh.textInputLayout.hint = keyList[position].capitalize()
        vh.param.tag = keyList[position]
        vh.param.setText(valueList[position])
        vh.param.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                apiTask.params[vh.param.tag.toString()] = editable.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }

    override fun getItemCount(): Int = apiTask.params.size

    fun isAllParamsFilled(): Boolean {
        apiTask.params.forEach { if(it.value.isBlank()) return false }
        return true
    }

    fun getParams(): ApiTask {
        return apiTask
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var textInputLayout: TextInputLayout = itemView.findViewById(R.id.text_input_layout)
        var param: EditText = itemView.findViewById(R.id.edit_text)

    }
}