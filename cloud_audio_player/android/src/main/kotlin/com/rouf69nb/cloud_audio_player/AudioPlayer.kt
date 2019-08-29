package com.rouf69nb.cloud_audio_player


import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.R.attr.streamType
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.lang.Exception


class AudioPlayer(private val context: Context, private val listener: AudioPlayerListener) {
    private var timeTickerHandler: Handler
    private var exoPlayer: SimpleExoPlayer
    private val audioManager: AudioManager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
    private var status: PlaybackStatus
    private var streamUrl: String? = null

    private var shouldUpdatePlaybackProgress= false
    private var shouldUpdateBufferProgress= false

    init {
        // initialize player
        status = PlaybackStatus.IDLE
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector(AdaptiveTrackSelection.Factory()))


        // initialize ticker handler
        timeTickerHandler = Handler()
        waitToDoSomethingRecursively()


//        //volume observer
//        context.applicationContext.contentResolver.registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, object : ContentObserver(Handler()){
//            override fun onChange(selfChange: Boolean) {
//                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
//
//                Log.d("Volume changed", "Volume now $currentVolume")
//            }
//        })

        // exo player listeners
        exoPlayer.addListener(object : Player.EventListener{
            override fun onPlayerError(error: ExoPlaybackException?) {
                status = PlaybackStatus.ERROR
                listener.onStatusChanged(status)
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                shouldUpdateBufferProgress = isLoading
                listener.onBufferingChanged(exoPlayer.bufferedPercentage.toFloat()/100f)
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                updatePlayerStateChanged(playbackState, playWhenReady)
            }
        })
    }
    fun dispose(){
        exoPlayer.stop()
        timeTickerHandler.removeCallbacksAndMessages(null)
        exoPlayer.release()
        //context.applicationContext.contentResolver.unregisterContentObserver(_volumeObserver)
    }
    private fun waitToDoSomethingRecursively() {
        timeTickerHandler.postDelayed(::onTimerTick, 200)
    }
    private fun onTimerTick(){
        updatePlaybackProgress()
        updateBufferProgress()

        //call again
        waitToDoSomethingRecursively()
    }



    private fun updatePlayerStateChanged(playbackState: Int, playWhenReady: Boolean) {
        status = when (playbackState) {
            Player.STATE_BUFFERING -> PlaybackStatus.LOADING
            Player.STATE_ENDED -> PlaybackStatus.COMPLETED
            Player.STATE_READY -> if (playWhenReady) {
                shouldUpdatePlaybackProgress = true
                PlaybackStatus.PLAYING
            } else {
                shouldUpdatePlaybackProgress = false
                PlaybackStatus.PAUSED
            }
            else -> PlaybackStatus.IDLE
        }
        //Invoke listener
        listener.onStatusChanged(status)
    }
    private fun updatePlaybackProgress(){
        if(shouldUpdatePlaybackProgress && exoPlayer.duration > 0) {
            val progressInPercent = (exoPlayer.currentPosition.toFloat() * 100f).toFloat() / exoPlayer.duration.toFloat()
            listener.onProgressChanged(progressInPercent)
        }
    }
    private fun updateBufferProgress(){
        if(shouldUpdateBufferProgress) {
            listener.onBufferingChanged(exoPlayer.bufferedPercentage.toFloat()/100f)
        }
    }




    // get player status
    var getStatus:PlaybackStatus = status
        get() = status
        private set

    // create media source
    private fun buildMediaSource(streamUrl: String, dataSourceFactory: DefaultDataSourceFactory): MediaSource {
        val uri = Uri.parse(streamUrl)
        return when (val type = Util.inferContentType(uri)) {
            C.TYPE_HLS   -> HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory) .createMediaSource(uri)
            else -> {
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }
    fun play(streamUrl: String) {
        this.streamUrl = streamUrl

        try {
            val dataSourceFactory = DefaultDataSourceFactory(context, Util.DEVICE)
            val mediaSource = buildMediaSource(streamUrl, dataSourceFactory)

            exoPlayer.stop()
            exoPlayer.prepare(mediaSource)
            exoPlayer.playWhenReady = requestAudioFocus()
            listener.onProgressChanged(0f)
        }catch (e:Exception){
            status = PlaybackStatus.ERROR
            listener.onStatusChanged(status)
            Log.e("AudioPlayer", "Media source error")
        }
    }
    fun pause() {
        if(exoPlayer.playWhenReady) exoPlayer.playWhenReady = false
    }
    fun resume() {
        if(!exoPlayer.playWhenReady) exoPlayer.playWhenReady = true
    }
    fun stop(){
        exoPlayer.stop()
        shouldUpdatePlaybackProgress = false
        shouldUpdateBufferProgress = false
        status = PlaybackStatus.STOPPED
        listener.onStatusChanged(status)
        listener.onBufferingChanged(0f)
        listener.onProgressChanged(0f)
    }


    // Settings
    fun setVolume(value: Float){
        if(value in 0.0..1.0)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * value).toInt(), 0)
    }
    fun setSpeed(value: Float){
        if(value in 0.5..2.5) {
            val param = PlaybackParameters(value)
            exoPlayer.playbackParameters = param
        }
    }
    fun seekTo(value: Float){
        if(exoPlayer.duration >0 && value in 0.0..1.0) exoPlayer.seekTo((exoPlayer.duration * value).toLong())
    }




    //====================== AUDIO FOCUS =========================
    //Audio Focus Listener
    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                exoPlayer.volume = 1.0f
                resume()
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                stop()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                exoPlayer.volume = 0.15f
            }
        }
    }
    // audio focus request method
    private fun requestAudioFocus(): Boolean {
        val r: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setAudioAttributes(
                                    AudioAttributes.Builder()
                                            .setLegacyStreamType(streamType)
                                            .build()
                            )
                            .setOnAudioFocusChangeListener(audioFocusListener)
                            .build()
            )
        }
        else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }

        return r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
}

//Status Object
enum class PlaybackStatus {
    IDLE, LOADING, PLAYING, PAUSED, STOPPED, ERROR, COMPLETED
}
//Listener definition
interface AudioPlayerListener{
    fun onStatusChanged(status:PlaybackStatus)
    fun onBufferingChanged(bufferPercent:Float)
    fun onProgressChanged(percent:Float)
}

//
//class SettingsContentObserver(context: Context, handler: Handler) : ContentObserver(handler) {
//    private val audioManager: AudioManager
//
//    init {
//        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//    }
//
//    override fun deliverSelfNotifications(): Boolean {
//        return false
//    }
//
//    override fun onChange(selfChange: Boolean) {
//        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
//
//       // Log.d(TAG, "Volume now $currentVolume")
//    }
//}