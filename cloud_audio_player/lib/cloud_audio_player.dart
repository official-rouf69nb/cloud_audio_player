import 'dart:async';

import 'package:flutter/services.dart';
import 'cloud_player_state.dart';

class CloudAudioPlayer {
  static const MethodChannel _channel = const MethodChannel('com.rouf69nb.cloud_audio_player/player');
  Function(CloudPlayerState status) _statusListener;
  Function(double bufferedPercent) _bufferListener;
  Function(double progressPercent, int totalDuration, int currentDuration) _progressListener;


  CloudAudioPlayer(){
    _channel.setMethodCallHandler(_playerEventHandler);
  }
  void addListeners({
    Function(CloudPlayerState status) statusListener,
    Function(double bufferedPercent) bufferListener,
    Function(double progressPercent, int totalDuration, int currentDuration) progressListener,
  }){
    _statusListener = statusListener;
    _bufferListener = bufferListener;
    _progressListener = progressListener;
  }
  static Future<double> getDeviceMediaVolume(){
    return _channel.invokeMethod("getVolume");
  }
  static void setVolume(double volume){
    if(volume >= 0 && volume <= 1.0)
    {
      _channel.invokeMethod("setVolume",{"volume":volume});
    }else
    {
      print("************************************\nVolume should between 0.0 to 1.0\n************************************");
    }
  }


  void play(String url){
    if(url.isNotEmpty)
    {
      _channel.invokeMethod("play",{"url":url});
    }
  }
  void pause(){
    _channel.invokeMethod("pause");
  }
  void resume(){
    _channel.invokeMethod("resume");
  }
  void stop(){
    _channel.invokeMethod("stop");
  }

  void setSpeed(double speed){
    if(speed >= 0.3 && speed <= 3.0)
    {
      _channel.invokeMethod("setSpeed",{"speed":speed});
    }else
    {
      print("************************************\nSpeed value should between 0.3 to 3.0\n************************************");
    }
  }
  void seekToPercent(double value){
    if(value >= 0.0 && value <= 1.0)
    {
      _channel.invokeMethod("seekTo",{"seekTo":value});
    }else
    {
      print("************************************\nSeek value should between 0.0 to 1.0\n************************************");
    }
  }
  Future<String> getStatus(){
    return _channel.invokeMethod("getStatus");
  }

  Future<int> getTotalDuration(){
    return _channel.invokeMethod("getTotalDuration");
  }

  Future<int> getCurrentPosition(){
    return _channel.invokeMethod("getCurrentPosition");
  }



  //=============== player event ===============
  Future<void> _playerEventHandler(MethodCall call) async{
    switch (call.method) {
      case "onStatusChanged":
        _statusListener?.call(getPlayerStateFromString(call.arguments));
        break;
      case "onBufferingChanged":
        _bufferListener?.call(call.arguments);
        break;
      case "onProgressChanged":
        {
          double _progress = call.arguments["progressPercent"];
          int _totalDuration= call.arguments["totalDuration"];
          int _currentPosition = call.arguments["currentPosition"];
          _progressListener?.call(_progress,_totalDuration < 0 ? 0:_totalDuration,_currentPosition <0 ? 0:_currentPosition);
        }
        break;
      default:
        print("********Others method*********** => ${call.arguments}");
        break;
    }
  }
}
