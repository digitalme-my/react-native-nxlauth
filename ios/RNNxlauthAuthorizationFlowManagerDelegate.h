#import <Foundation/Foundation.h>

@protocol RNNxlauthAuthorizationFlowManagerDelegate <NSObject>
@required
-(BOOL)resumeExternalUserAgentFlowWithURL:(NSURL *)url;
@end
