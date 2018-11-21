#import <React/RCTBridgeModule.h>

@class OIDAuthState;
@interface RNNxlauth : NSObject <RCTBridgeModule>

@property(nonatomic, strong, nullable) OIDAuthState *authState;


@end
  
