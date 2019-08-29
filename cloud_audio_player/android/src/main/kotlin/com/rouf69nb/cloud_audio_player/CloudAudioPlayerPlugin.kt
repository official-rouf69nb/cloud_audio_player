package com.rouf69nb.cloud_audio_player

import android.content.Context
import android.media.AudioManager
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar




class CloudAudioPlayerPlugin(registrar: Registrar, private val channel: MethodChannel) : MethodCallHandler, AudioPlayerListener {
  private val player: AudioPlayer = AudioPlayer(registrar.context().applicationContext,this)
  private val audioManager:AudioManager = registrar.context().applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager


  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "com.rouf69nb.cloud_audio_player/player")
      channel.setMethodCallHandler(CloudAudioPlayerPlugin(registrar, channel))
    }
  }
  override fun onMethodCall(call: MethodCall, result: Result) {
    when(call.method){
      "play"->{
        player.play(call.argument<String>("url")!!)
        result.success(null)
      }
      "pause"->{
        player.pause()
        result.success(null)
      }
      "resume"->{
        player.resume()
        result.success(null)
      }
      "stop"->{
        player.stop()
        result.success(null)
      }
      "setVolume"->{
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * call.argument<Float>("volume")!!).toInt(), 0)
        result.success(null)
      }
      "setSpeed"->{
        player.setSpeed(call.argument<Float>("speed")!!)
        result.success(null)
      }
      "seekTo"->{
        player.seekTo(call.argument<Float>("seekTo")!!)
        result.success(null)
      }
      "getVolume"->{
        val volume = (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100).toFloat() / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        result.success(volume)
      }
      "getStatus"->{
        result.success(player.getStatus.name)
      }
      "getTotalDuration"->{
        result.success(player.getTotalDuration)
      }
      "getCurrentPosition"->{
        result.success(player.getCurrentPosition)
      }
      "dispose"->{
        player.dispose()
        result.success(null)
      }
      else ->result.success(null)
    }
  }



  // ================= Player event =================
  override fun onStatusChanged(status: PlaybackStatus) {
    channel.invokeMethod("onStatusChanged",status.name)
  }
  override fun onBufferingChanged(bufferPercent: Float) {
    channel.invokeMethod("onBufferingChanged",bufferPercent)
  }
  override fun onProgressChanged(percent: Float, totalDuration:Long, currentPosition:Long) {
    channel.invokeMethod("onProgressChanged",hashMapOf("progressPercent" to percent, "totalDuration" to totalDuration, "currentPosition" to currentPosition))
  }
}
