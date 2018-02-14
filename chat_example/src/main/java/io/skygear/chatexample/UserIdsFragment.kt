package io.skygear.chatexample

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import io.skygear.plugins.chat.ChatContainer
import io.skygear.plugins.chat.ChatUser
import io.skygear.plugins.chat.GetCallback
import io.skygear.plugins.chat.GetChatUsersCallback
import io.skygear.skygear.Container
import io.skygear.skygear.Error
import java.util.* // ktlint-disable no-wildcard-imports

class UserIdsFragment : DialogFragment() {
    private var mListener: (List<String>) -> Unit = {}

    private val mSkygear: Container
    private val mChatContainer: ChatContainer

    private var mAdapter: UserIdsAdapter? = null
    private var mUserIdsRv: RecyclerView? = null

    companion object {
        private val TITLE_KEY = "title_key"
        private val SELECT_IDS_KEY = "selected_ids_key"

        fun newInstance(title: String, selected: Set<String>?): UserIdsFragment {
            val f = UserIdsFragment()
            val args = Bundle()
            args.putString(TITLE_KEY, title)
            args.putStringArrayList(SELECT_IDS_KEY, ArrayList(selected))
            f.arguments = args

            return f
        }
    }

    init {
        mSkygear = Container.defaultContainer(activity)
        mChatContainer = ChatContainer.getInstance(mSkygear)
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_user_ids, container)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mUserIdsRv = view?.findViewById<RecyclerView>(R.id.user_ids_rv)
        mAdapter = UserIdsAdapter(mSkygear.auth.currentUser?.id)
        mUserIdsRv?.adapter = mAdapter
        mUserIdsRv?.layoutManager = LinearLayoutManager(activity)

        val titleTv: TextView? = view?.findViewById<TextView>(R.id.title_tv)
        val title: String? = arguments?.getString(TITLE_KEY)
        if (title != null && !title.isNullOrEmpty()) {
            titleTv?.text = SpannableStringBuilder(title)
        }

        val okBtn: Button? = view?.findViewById<Button>(R.id.ok_btn)
        okBtn?.setOnClickListener {
            mListener(mAdapter!!.getSelectedIds())
            dismiss()
        }
    }

    override fun onResume() {
        val params = dialog.window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window.attributes = params

        super.onResume()

        mChatContainer.getChatUsers(object : GetChatUsersCallback {
            override fun onGetCachedResult(participantsMap: MutableMap<String, ChatUser>?) {
                mAdapter?.setUserIds(participantsMap?.values?.toList(), arguments.getStringArrayList(SELECT_IDS_KEY))
            }

            override fun onFail(error: Error) {

            }

            override fun onSuccess(participantsMap: MutableMap<String, ChatUser>?) {
                mAdapter?.setUserIds(participantsMap?.values?.toList(), arguments.getStringArrayList(SELECT_IDS_KEY))
            }
        })
    }

    fun setOnOkBtnClickedListener(listener: (List<String>) -> Unit) {
        mListener = listener
    }
}
