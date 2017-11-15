package io.skygear.plugins.chat.ui.utils

import android.util.Log
import io.skygear.plugins.chat.ChatContainer
import io.skygear.plugins.chat.ChatUser
import io.skygear.plugins.chat.ui.model.User
import io.skygear.plugins.chat.ui.model.UserBuilder
import io.skygear.skygear.*
import java.util.*

class UserCache(val skygear: Container, val userBuilder: UserBuilder) {
    companion object {
        private val TAG = UserCache::class.java.canonicalName
        private var sharedInstance: UserCache? = null

        fun getInstance(skygear: Container, userBuilder: UserBuilder): UserCache {
            if (this.sharedInstance == null) {
                this.sharedInstance = UserCache(skygear, userBuilder)
            }

            return this.sharedInstance as UserCache
        }
    }

    private val cacheMap = HashMap<String, User>()

    private fun cache(user: User) {
        this.cacheMap[user.chatUser.record.id] = user
    }

    fun getUsers(
            userIDs: List<String>,
            callback: ((users: Map<String, User>) -> Unit)?
    ) {
        val invokeCallback = {
            callback?.let { cb ->
                val users = HashMap<String, User>()
                userIDs.forEach { perID ->
                    this.cacheMap[perID]?.let { theUser ->
                        users[perID] = theUser
                    }
                }

                cb(users)
            }
        }

        val filtered = userIDs.filter { !this.cacheMap.containsKey(it) }
        if (filtered.isEmpty()) {
            invokeCallback()
            return
        }

        val q = Query("user").contains("_id", filtered)
        this.skygear.publicDatabase.query(q, object: RecordQueryResponseHandler(){
            override fun onQueryError(error: Error?) {
                Log.w(TAG, "Failed to query users: ${error?.message}")
            }

            override fun onQuerySuccess(records: Array<out Record>?) {
                records?.map { userBuilder.createUser(it) }
                        ?.forEach { this@UserCache.cache(it) }
                invokeCallback()
            }

        })
    }
}