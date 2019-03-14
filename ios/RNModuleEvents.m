
#import "RNModuleEvents.h"
#import "RNEventCallback.h"

@interface RNModuleEvents() {
	NSMutableDictionary<NSString*, NSMutableArray<RNEventCallback*>*>* _eventListeners;
}
@end


@implementation RNModuleEvents

-(id)init {
	if(self = [super init]) {
		_eventListeners = [NSMutableDictionary dictionary];
	}
	return self;
}

-(void)addListener:(void(^)(NSArray*))listener forEvent:(NSString*)event onlyOnce:(BOOL)once {
	@synchronized (_eventListeners) {
		NSMutableArray<RNEventCallback*>* listeners = _eventListeners[event];
		if (listeners == nil) {
			listeners = [[NSMutableArray alloc] init];
			_eventListeners[event] = listeners;
		}
		[listeners addObject:[[RNEventCallback alloc] initWithBlock:listener callOnlyOnce:once]];
	}
}

-(void)prependListener:(void(^)(NSArray*))listener forEvent:(NSString*)event onlyOnce:(BOOL)once {
	@synchronized (_eventListeners) {
		NSMutableArray<RNEventCallback*>* listeners = _eventListeners[event];
		if (listeners == nil) {
			listeners = [[NSMutableArray alloc] init];
			_eventListeners[event] = listeners;
		}
		[listeners insertObject:[[RNEventCallback alloc] initWithBlock:listener callOnlyOnce:once] atIndex:0];
	}
}

-(void)removeListener:(void(^)(NSArray*))listener forEvent:(NSString*)event {
	@synchronized (_eventListeners) {
		NSMutableArray<RNEventCallback*>* listeners = _eventListeners[event];
		for (NSUInteger i=0; i<listeners.count; i++) {
			RNEventCallback* callback = listeners[i];
			if (callback.block == listener) {
				[listeners removeObjectAtIndex:i];
				return;
			}
		}
	}
}

-(void)removeAllListenersForEvent:(NSString*)event {
	@synchronized (_eventListeners) {
		if(event == nil) {
			[_eventListeners removeAllObjects];
		}
		else {
			[_eventListeners removeObjectForKey:event];
		}
	}
}

-(void)removeAllListeners {
	@synchronized (_eventListeners) {
		[_eventListeners removeAllObjects];
	}
}

-(NSUInteger)listenerCountForEvent:(NSString*)event {
	@synchronized (_eventListeners) {
		NSMutableArray<RNEventCallback*>* listeners = _eventListeners[event];
		if (listeners == nil) {
			return 0;
		}
		return listeners.count;
	}
}

-(BOOL)emitEvent:(NSString*)event withParams:(NSArray*)params {
	NSArray<RNEventCallback*>* tmpListeners = nil;
	
	@synchronized (_eventListeners) {
		NSMutableArray<RNEventCallback*>* listeners = _eventListeners[event];
		if(listeners != nil) {
			tmpListeners = [NSMutableArray arrayWithArray:listeners];
		}
		
		// remove "once" event listeners
		if(listeners != nil) {
			for (NSUInteger i=0; i<listeners.count; i++) {
				RNEventCallback* listener = listeners[i];
				if (listener.calledOnlyOnce) {
					[listeners removeObjectAtIndex:i];
					i--;
				}
			}
		}
	}
	
	if (tmpListeners != nil && tmpListeners.count > 0) {
		//invoke events
		for(NSUInteger i=0; i<tmpListeners.count; i++) {
			RNEventCallback* listener = tmpListeners[i];
			listener.block(params);
		}
		return true;
	}
	return false;
}

@end
