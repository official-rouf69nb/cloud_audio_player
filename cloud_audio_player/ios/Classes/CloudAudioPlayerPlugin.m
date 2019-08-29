#import "CloudAudioPlayerPlugin.h"
#import <cloud_audio_player/cloud_audio_player-Swift.h>

@implementation CloudAudioPlayerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftCloudAudioPlayerPlugin registerWithRegistrar:registrar];
}
@end
