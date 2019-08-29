enum CloudPlayerState {
  IDLE,
  LOADING,
  PLAYING,
  PAUSED,
  STOPPED,
  COMPLETED,
  ERROR
}

CloudPlayerState getPlayerStateFromString(String state){
  CloudPlayerState _state= CloudPlayerState.IDLE;
  switch(state){
    case "LOADING":
      _state = CloudPlayerState.LOADING;
      break;
    case "PLAYING":
      _state = CloudPlayerState.PLAYING;
      break;
    case "PAUSED":
      _state = CloudPlayerState.PAUSED;
      break;
    case "STOPPED":
      _state = CloudPlayerState.STOPPED;
      break;
    case "COMPLETED":
      _state = CloudPlayerState.COMPLETED;
      break;
    case "ERROR":
      _state = CloudPlayerState.ERROR;
      break;
    default:
      _state = CloudPlayerState.IDLE;
      break;
  }
  return _state;
}