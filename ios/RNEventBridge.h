
#import <Foundation/Foundation.h>

@class RCTBridge;

@interface RNEventBridge : NSObject

+(id)moduleForClass:(Class)classObj bridge:(RCTBridge*)bridge;
+(void)sendEvent:(NSString*)event body:(id)body bridge:(RCTBridge*)bridge;

@end
