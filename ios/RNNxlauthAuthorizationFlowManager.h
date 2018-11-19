#import <Foundation/Foundation.h>
#import "RNNxlauthAuthorizationFlowManagerDelegate.h"

@protocol RNNxlauthAuthorizationFlowManager <NSObject>
@required
@property(nonatomic, weak)id<RNNxlauthAuthorizationFlowManagerDelegate>authorizationFlowManagerDelegate;
@end
