package io.skygear.chatexample

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import io.skygear.plugins.chat.UserConversation

class MetaFragment : DialogFragment() {
    private var mListener: (String) -> Unit = {}

    companion object {
        private val UNREAD_CNT = "unread_count"

        fun newInstance(userConversation: UserConversation?): MetaFragment {
            val f = MetaFragment()
            val args = Bundle()
            args.putString(UNREAD_CNT, userConversation?.unreadCount.toString())
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

        val titleEt: TextView? = view?.findViewById(R.id.unread_count_tv) as TextView
        val unread: String? = arguments?.getString(UNREAD_CNT)
        titleEt?.text = unread

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
