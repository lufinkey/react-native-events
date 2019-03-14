
#import "RNEventBridge.h"

#if __has_include("RCTBridge.h")
#import "RCTBridge.h"
#else
#import <React/RCTBridge.h>
#endif

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
