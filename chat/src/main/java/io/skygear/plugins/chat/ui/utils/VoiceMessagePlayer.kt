package io.skygear.plugins.chat.ui.utils

import android.media.MediaPlayer
import io.skygear.plugins.chat.ui.model.VoiceMessage

class VoiceMessagePlayer {
    companion object {
        private val TAG = VoiceMessagePlayer::class.java.canonicalName
    }
    private var mediaPlayer: MediaPlayer? = null

    var messageStateChangeListener: OnMessageStateChangeListener? = null
    var message: VoiceMessage? = null

    fun play() {
        this.message?.let { msg ->
            if (msg.state == VoiceMessage.State.PAUSED) {
                this.mediaPlayer?.start()
                msg.state = VoiceMessage.State.PLAYING

                this.messageStateChangeListener?.onVoiceMessageStateChanged(msg)
                return
            }

            this.mediaPlayer = MediaPlayer()
            this.mediaPlayer?.setDataSource(msg.attachmentUrl)
            this.mediaPlayer?.setOnCompletionListener {
                msg.state = VoiceMessage.State.INITIAL
                this.messageStateChangeListener?.onVoiceMessageStateChanged(msg)
            }

            this.mediaPlayer?.prepare()
            this.mediaPlayer?.start()

            msg.state = VoiceMessage.State.PLAYING
            this.messageStateChangeListener?.onVoiceMessageStateChanged(msg)
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

    interface OnMessageStateChangeListener {
        fun onVoiceMessageStateChanged(voiceMessage: VoiceMessage)
    }
}
