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
import io.skygear.plugins.chat.Participant
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.GetParticipantsCallback
import io.skygear.skygear.Container
import io.skygear.skygear.Error
import java.util.* // ktlint-disable no-wildcard-imports

class UserIdsFragment : DialogFragment() {
    private var mListener: (List<String>) -> Unit = {}

    private val mSkygear: Container
    private val mChatContainer: ChatContainer
    private var mConversation: Conversation? = null
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

        mChatContainer.getParticipants(object : GetParticipantsCallback {
            override fun onGetCachedResult(participantsMap: MutableMap<String, Participant>?) {
                mAdapter?.setUserIds(participantsMap?.values?.toList(), arguments.getStringArrayList(SELECT_IDS_KEY))
            }

            override fun onFail(error: Error) {

            }

            override fun onSuccess(participantsMap: MutableMap<String, Participant>?) {
                if (mConversation != null) {
                    var allUserList = participantsMap?.values?.toMutableList()
                    // Only display Participant here if a conversation is specified
                    // Remove those not contained in participant IDs
                    allUserList?.removeAll {
                        !mConversation?.participantIds?.contains(it.id)!!
                    }
                    mAdapter?.setUserIds(allUserList, arguments.getStringArrayList(SELECT_IDS_KEY))
                } else {
                    mAdapter?.setUserIds(participantsMap?.values?.toList(), arguments.getStringArrayList(SELECT_IDS_KEY))
                }
            }
        })
    }

    fun setConversation(conversation: Conversation) {
        mConversation = conversation
    }

    fun setOnOkBtnClickedListener(listener: (List<String>) -> Unit) {
        mListener = listener
    }
}
