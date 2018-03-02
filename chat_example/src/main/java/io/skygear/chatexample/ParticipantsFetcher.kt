package io.skygear.chatexample


import io.skygear.plugins.chat.GetParticipantsCallback
import io.skygear.plugins.chat.Participant
import io.skygear.skygear.*
import java.util.*


open class ParticipantsFetcher {
    private val container: Container
    constructor(container: Container) {
        this.container = container
    }

    open fun fetch(callback: GetParticipantsCallback) {
        if (callback != null) {
            val publicDB = this.container.publicDatabase
            val query = Query("user")
            //TODO: unset limit after https://github.com/SkygearIO/skygear-SDK-Android/issues/210 is closed
            query.limit = 999
            publicDB.query(query, object : RecordQueryResponseHandler() {
                override fun onQueryError(error: Error?) {
                    error?.let {
                        callback.onFail(error)
                    }
                }

                override fun onQuerySuccess(records: Array<out Record>?) {
                    val participants = records?.map { p -> Participant(p)}?.associateBy({ it.id }, { it })
                    callback.onSuccess(participants)
                }
            })
        }
    }

}