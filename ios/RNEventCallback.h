
#import <Foundation/Foundation.h>

@interface RNEventCallback : NSObject

-(id)initWithBlock:(void(^)())block callOnlyOnce:(BOOL)once;

@property (readonly) void(^block)();
@property (readonly, getter=isCalledOnlyOnce) BOOL calledOnlyOnce;

@end
