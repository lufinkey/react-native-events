
#import "RNEventCallback.h"

@implementation RNEventCallback

-(id)initWithBlock:(void(^)())block callOnlyOnce:(BOOL)once {
	if(self = [super init]) {
		_block = block;
		_calledOnlyOnce = once;
	}
	return self;
}

@end
