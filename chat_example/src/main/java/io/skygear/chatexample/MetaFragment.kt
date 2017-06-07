package io.skygear.chatexample

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import io.skygear.plugins.chat.Conversation

class MetaFragment : DialogFragment() {
    private var mListener: (String) -> Unit = {}

    companion object {
        private val UNREAD_CNT = "unread_count"
        private val LAST_READ_MESSAGE = "last_read_message"

        fun newInstance(conversation: Conversation?): MetaFragment {
            val f = MetaFragment()
            val args = Bundle()
            args.putString(UNREAD_CNT, conversation?.unreadCount.toString())
            args.putString(LAST_READ_MESSAGE, conversation?.lastReadMessage?.id)
            f.arguments = args

            return f
        }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_meta, container)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTv: TextView? = view?.findViewById(R.id.unread_count_tv) as TextView
        val unread: String? = arguments?.getString(UNREAD_CNT)
        titleTv?.text = unread

        val lastMsgTv: TextView? = view?.findViewById(R.id.last_read_message_tv) as TextView
        val lasMsg: String? = arguments?.getString(LAST_READ_MESSAGE)
        lastMsgTv?.text = lasMsg

        val okBtn: Button? = view?.findViewById(R.id.ok_btn) as Button
        okBtn?.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        val params = dialog.window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window.attributes = params

        super.onResume()
    }

}
