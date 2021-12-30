
#import "RNEventBridge.h"

#import <React/RCTBridge.h>

@implementation RNEventBridge

+(id)moduleForClass:(Class)classObj bridge:(RCTBridge*)bridge {
	return [bridge moduleForClass:classObj];
}

+(void)sendEvent:(NSString*)event body:(id)body bridge:(RCTBridge*)bridge {
	[bridge enqueueJSCall:@"RCTDeviceEventEmitter"
				   method:@"emit"
					 args:body ? @[event, body] : @[event]
			   completion:nil];
}

@end
