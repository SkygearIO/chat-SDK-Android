package io.skygear.chatexample

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText

class TitleFragment : DialogFragment() {
    private var mListener: (String) -> Unit = {}

    companion object {
        private val DEF_INPUT_KEY = "text_key"

        fun newInstance(defInput: String?): TitleFragment {
            val f = TitleFragment()
            val args = Bundle()
            args.putString(DEF_INPUT_KEY, defInput)
            f.arguments = args

            return f
        }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_title, container)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleEt: EditText? = view?.findViewById<EditText>(R.id.title_et)
        val def: String? = arguments?.getString(DEF_INPUT_KEY)
        if (def != null && !def.isNullOrEmpty()) {
            titleEt?.text = SpannableStringBuilder(def)
            titleEt?.setSelection(def.length)
        }

        val okBtn: Button? = view?.findViewById<Button>(R.id.ok_btn)
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
