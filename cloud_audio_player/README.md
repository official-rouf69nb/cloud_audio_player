# cloud_audio_player

A Flutter plugin for playing audio files from remote URL . Currently supports on only Android.

## Getting Started

 ###### To use this plugin, add cloud_audio_player as a [dependency in your pubspec.yaml file.](https://flutter.dev/docs/development/packages-and-plugins/using-packages)


### Example

````dart in html
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
  
  
  //Player Events
  _onStatusChanged(PlayerState status) {
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
````