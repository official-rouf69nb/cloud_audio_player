import 'package:cloud_audio_player/cloud_player_state.dart';
import 'package:flutter/material.dart';
import 'package:cloud_audio_player/cloud_audio_player.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  CloudAudioPlayer _player;

  String _statusText ="";
  double _progress = 0;
  double _buffered = 0;

  @override
  void initState() {
    super.initState();
    _player = CloudAudioPlayer();
    _player.addListeners(
      statusListener: _onStatusChanged,
      bufferListener: _onBufferUpdate,
      progressListener: _onProgressUpdate,
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        backgroundColor: Colors.white,
        appBar: AppBar(
          title: const Text('Audio Player example app'),
        ),
        body: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                Text(
                  _statusText,
                  style: TextStyle(
                    color: Colors.green,
                    fontSize: 26,
                    fontWeight: FontWeight.bold,
                  ),
                  textAlign: TextAlign.center,
                ),
                SizedBox(height: 8,),
                Text(
                  "Buffered ${(_buffered * 100).round()}%",
                  style: TextStyle(
                    color: Colors.green,
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                  textAlign: TextAlign.center,
                ),
                Text(
                  "Played ${(_progress * 100).round()}%",
                  style: TextStyle(
                    color: Colors.green,
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                  textAlign: TextAlign.center,
                ),
                SizedBox(height: 32,),

                SizedBox(height: 16,),
                LinearProgressIndicator(
                  backgroundColor: Colors.grey,
                  valueColor: AlwaysStoppedAnimation(Colors.red),
                  value: _progress,
                ),
                SizedBox(height: 16,),
                Row(children: <Widget>[
                  Expanded(
                    child: RaisedButton(
                      onPressed: _onLoad,
                      color: Colors.orange,
                      child: Text("Load",style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),),
                    ),
                  ),
                  SizedBox(width: 8,),
                  Expanded(
                    child: RaisedButton(
                      onPressed: _onPause,
                      color: Colors.orange,
                      child: Text("Pause",style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),),
                    ),
                  ),
                  SizedBox(width: 8,),
                  Expanded(
                    child: RaisedButton(
                      onPressed: _onResume,
                      color: Colors.orange,
                      child: Text("Play",style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),),
                    ),
                  ),
                  SizedBox(width: 8,),
                  Expanded(
                    child: RaisedButton(
                      onPressed: _onStop,
                      color: Colors.orange,
                      child: Text("Stop",style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),),
                    ),
                  ),
                ],),
                Row(children: <Widget>[
                  Expanded(
                    child: RaisedButton(
                      onPressed: _onPrevious,
                      color: Colors.orange,
                      child: Text("Previous",style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),),
                    ),
                  ),
                  SizedBox(width: 8,),
                  Expanded(
                    child: RaisedButton(
                      onPressed: _onNext,
                      color: Colors.orange,
                      child: Text("Next",style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),),
                    ),
                  ),
                ],),
              ],
            ),
          ),
        ),
      ),
    );
  }


  void _onLoad() {
    _player.play("https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_5MG.mp3");
  }
  void _onResume() {
    _player.resume();
  }
  void _onPause() {
    _player.pause();
  }
  void _onStop() {
    _player.stop();
  }



  //================== PLAYER EVENT ================
  //Player Event
  _onStatusChanged(CloudPlayerState status) {
    print(status);
    setState(() {
      _statusText = status.toString();
    });
  }
  _onBufferUpdate(double bufferedPercent) {
    setState(() {
      _buffered =bufferedPercent;
    });
  }
  _onProgressUpdate(double progressPercent) {
    setState(() {
      _progress =progressPercent / 100;
    });
  }

  void _onNext() {
    _player.play("https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_5MG.mp3");
  }

  void _onPrevious() {
    _player.play("https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_5MG.mp3");
  }


}
