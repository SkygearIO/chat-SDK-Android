package io.skygear.chatexample

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText

class CreateConversationTitleFragment : DialogFragment() {
    private var mListener: (String) -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_create_conversation_title, container)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleEt: EditText? = view?.findViewById(R.id.title_et) as EditText
        val okBtn: Button? = view?.findViewById(R.id.ok_btn) as Button
        okBtn?.setOnClickListener {
            mListener(titleEt?.text.toString())
            dismiss()
        }
    }

    override fun onResume() {
        val params = dialog.window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window.attributes = params

        super.onResume()
    }

    fun setOnOkBtnClickedListener(listener: (String) -> Unit) {
        mListener = listener
    }
}


