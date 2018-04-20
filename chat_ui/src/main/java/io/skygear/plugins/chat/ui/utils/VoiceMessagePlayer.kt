package io.skygear.plugins.chat.ui.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.* // ktlint-disable no-wildcard-imports
import io.skygear.plugins.chat.ui.model.VoiceMessage

class VoiceMessagePlayer(val context: Context) {
    companion object {
        private val TAG = VoiceMessagePlayer::class.java.canonicalName
    }
    private var mediaPlayer: MediaPlayer? = null

    var messageStateChangeListener: OnMessageStateChangeListener? = null
    var playerErrorListener: OnPlayerErrorListener? = null
    var message: VoiceMessage? = null

    fun play() {
        this.message?.let { msg ->
            val url = msg.attachmentUrl ?: return

            if (msg.state == VoiceMessage.State.PAUSED) {
                this.mediaPlayer?.start()
                msg.state = VoiceMessage.State.PLAYING

                this.messageStateChangeListener?.onVoiceMessageStateChanged(msg)
                return
            }

            this.mediaPlayer = MediaPlayer()

            this.mediaPlayer?.setDataSource(url)

            this.mediaPlayer?.setOnCompletionListener {
                msg.state = VoiceMessage.State.INITIAL
                this.messageStateChangeListener?.onVoiceMessageStateChanged(msg)
            }

            this.mediaPlayer?.setOnErrorListener { _, what, _ ->
                msg.state = VoiceMessage.State.INITIAL
                this@VoiceMessagePlayer.playerErrorListener?.let { listener ->
                    when (what) {
                        MEDIA_ERROR_SERVER_DIED -> Error(MEDIA_ERROR_SERVER_DIED, "Server Error")
                        else -> Error(MEDIA_ERROR_UNKNOWN, "Unknown Error")
                    }.let { listener.onVoiceMessagePlayerError(it) }
                }

                false
            }

            this.mediaPlayer?.setOnPreparedListener { player ->
                player.start()
                msg.state = VoiceMessage.State.PLAYING
                this@VoiceMessagePlayer.messageStateChangeListener?.onVoiceMessageStateChanged(msg)
            }
            msg.state = VoiceMessage.State.PREPARING
            this.mediaPlayer?.prepareAsync()
        }
    }

    fun pause() {
        this.message?.let { msg ->
            this.mediaPlayer?.pause()

            msg.state = VoiceMessage.State.PAUSED
            this.messageStateChangeListener?.onVoiceMessageStateChanged(msg)
        }
    }

    fun stop() {
        this.message?.let { msg ->
            this.mediaPlayer?.stop()
            this.mediaPlayer?.release()
            this.mediaPlayer = null

            msg.state = VoiceMessage.State.INITIAL
            this.messageStateChangeListener?.onVoiceMessageStateChanged(msg)
        }

        this.message = null
    }

    class Error(val code: Int, message: String) : java.lang.Error(message)

    interface OnMessageStateChangeListener {
        fun onVoiceMessageStateChanged(voiceMessage: VoiceMessage)
    }

    interface OnPlayerErrorListener {
        fun onVoiceMessagePlayerError(error: Error)
    }
}
