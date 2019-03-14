
#import "RNEventEmitter.h"
#import "RNEventConformer.h"
#import "RNModuleEvents.h"
#import "RNEventBridge.h"

@interface RNEventEmitter() {
	NSMutableDictionary<NSNumber*, id<RNEventConformer>>* _registeredModules;
	NSMutableDictionary<NSNumber*, RNModuleEvents*>* _modules;
}
-(RNModuleEvents*)moduleEventsForID:(int)moduleId;
-(NSNumber*)registeredModuleIDForModule:(id<RNEventConformer>)module;
@end


@implementation RNEventEmitter

@synthesize bridge = _bridge;

static NSString* EVENT_NAME = @"ayylmao_dicksnshit_nobodyUsethisevent PLS OK THANKS";

RCT_EXPORT_MODULE()

-(id)init {
	if(self = [super init]) {
		_registeredModules = [NSMutableDictionary dictionary];
		_modules = [NSMutableDictionary dictionary];
	}
	return self;
}

+(BOOL)requiresMainQueueSetup {
	return NO;
}

+(RNEventEmitter*)eventEmitterForBridge:(RCTBridge*)bridge {
	return [RNEventBridge moduleForClass:[RNEventEmitter class] bridge:bridge];
}



+(void)registerEventEmitterModule:(id<RNEventConformer>)module withID:(int)moduleId bridge:(RCTBridge*)bridge {
	RNEventEmitter* eventEmitter = [self eventEmitterForBridge:bridge];
	if(eventEmitter == nil) {
		NSLog(@"Error: No RNEventEmitter is available to register to");
		return;
	}
	[eventEmitter registerModule:module withID:moduleId];
}

-(void)registerModule:(id<RNEventConformer>)module withID:(int)moduleId {
	@synchronized (_registeredModules) {
		if([[_registeredModules allValues] indexOfObjectIdenticalTo:module] != NSNotFound) {
			@throw [NSException exceptionWithName:NSInvalidArgumentException reason:@"Module has already been registered" userInfo:nil];
		}
		if(_registeredModules[@(moduleId)] != nil) {
			@throw [NSException exceptionWithName:NSInvalidArgumentException reason:@"moduleId has already been registered to a module" userInfo:nil];
		}
		_registeredModules[@(moduleId)] = module;
	}
}

-(RNModuleEvents*)moduleEventsForID:(int)moduleId {
	@synchronized (_modules) {
		RNModuleEvents* moduleEvents = _modules[@(moduleId)];
		if (moduleEvents == nil) {
			moduleEvents = [[RNModuleEvents alloc] init];
			_modules[@(moduleId)] = moduleEvents;
		}
		return moduleEvents;
	}
}

-(id<RNEventConformer>)registeredModuleForID:(int)moduleId {
	@synchronized (_registeredModules) {
		return _registeredModules[@(moduleId)];
	}
}

-(NSNumber*)registeredModuleIDForModule:(id<RNEventConformer>)module {
	@synchronized (_registeredModules) {
		for (NSNumber* moduleId in [_registeredModules allKeys]) {
			id<RNEventConformer> cmpModule = _registeredModules[moduleId];
			if(cmpModule == module) {
				return moduleId;
			}
		}
		return nil;
	}
}



+(void)emitEvent:(NSString*)event withParams:(NSArray*)params module:(id<RNEventConformer>)module bridge:(RCTBridge*)bridge {
	RNEventEmitter* eventEmitter = [self eventEmitterForBridge:bridge];
	if(eventEmitter == nil) {
		NSLog(@"Error: No RNEventEmitterModule is available to emit %@ event", event);
		return;
	}
	[eventEmitter emitEvent:event withParams:params forModule:module];
}

-(void)emitEvent:(NSString*)event withParams:(NSArray*)params forModule:(id<RNEventConformer>)module {
	NSNumber* moduleId = [self registeredModuleIDForModule:module];
	if(moduleId == nil) {
		NSLog(@"Error: Cannot emit %@ event before %@ module has been registered", event, module);
		return;
	}
	[[self moduleEventsForID:moduleId.intValue] emitEvent:event withParams:params];
	
	NSMutableDictionary* jsEvent = [NSMutableDictionary dictionary];
	jsEvent[@"moduleId"] = moduleId;
	jsEvent[@"eventName"] = event;
	jsEvent[@"args"] = params;
	
	[RNEventBridge sendEvent:EVENT_NAME body:jsEvent bridge:_bridge];
	
	if([module respondsToSelector:@selector(onEvent:params:)]) {
		[module onEvent:event params:params];
	}
	if([module respondsToSelector:@selector(onNativeEvent:params:)]) {
		[module onNativeEvent:event params:params];
	}
}

RCT_EXPORT_METHOD(emit:(int)moduleId event:(NSString*)event params:(NSArray*)params) {
	id<RNEventConformer> module = [self registeredModuleForID:moduleId];
	if(module == nil) {
		@throw [NSException exceptionWithName:NSInvalidArgumentException reason:@"No module registered with that ID" userInfo:nil];
	}
	
	[[self moduleEventsForID:moduleId] emitEvent:event withParams:params];
	
	if([module respondsToSelector:@selector(onEvent:params:)]) {
		[module onEvent:event params:params];
	}
	if([module respondsToSelector:@selector(onJSEvent:params:)]) {
		[module onJSEvent:event params:params];
	}
}



-(void)addListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModule:(id<RNEventConformer>)module runOnce:(BOOL)once {
	NSNumber* moduleId = [self registeredModuleIDForModule:module];
	if(moduleId == nil) {
		@throw [NSException exceptionWithName:NSInvalidArgumentException reason:@"Module has not been registered" userInfo:nil];
	}
	[self addListener:listener forEvent:event fromModuleWithID:moduleId.intValue runOnce:once];
}

-(void)addListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModule:(id<RNEventConformer>)module {
	[self addListener:listener forEvent:event fromModule:module runOnce:NO];
}

-(void)addListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModuleWithID:(int) moduleId runOnce:(BOOL)once {
	[[self moduleEventsForID:moduleId] addListener:listener forEvent:event onlyOnce:once];
}

-(void)addListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModuleWithID:(int)moduleId {
	[self addListener:listener forEvent:event fromModuleWithID:moduleId runOnce:NO];
}



-(void)prependListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModule:(id<RNEventConformer>)module runOnce:(BOOL)once {
	NSNumber* moduleId = [self registeredModuleIDForModule:module];
	if(moduleId == nil) {
		@throw [NSException exceptionWithName:NSInvalidArgumentException reason:@"Module has not been registered" userInfo:nil];
	}
	[self prependListener:listener forEvent:event fromModuleWithID:moduleId.intValue runOnce:once];
}

-(void)prependListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModule:(id<RNEventConformer>)module {
	[self prependListener:listener forEvent:event fromModule:module runOnce:NO];
}

-(void)prependListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModuleWithID:(int) moduleId runOnce:(BOOL)once {
	[[self moduleEventsForID:moduleId] addListener:listener forEvent:event onlyOnce:once];
}

-(void)prependListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModuleWithID:(int)moduleId {
	[self addListener:listener forEvent:event fromModuleWithID:moduleId runOnce:NO];
}



-(void)removeListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModule:(id<RNEventConformer>)module {
	NSNumber* moduleId = [self registeredModuleIDForModule:module];
	if(moduleId == nil)
	{
		@throw [NSException exceptionWithName:NSInvalidArgumentException reason:@"Module has not been registered" userInfo:nil];
	}
	[self removeListener:listener forEvent:event fromModuleWithID:moduleId.intValue];
}

-(void)removeListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModuleWithID:(int)moduleId {
	[[self moduleEventsForID:moduleId] removeListener:listener forEvent:event];
}



-(void)removeAllListenersFromModule:(id<RNEventConformer>)module forEvent:(NSString*)event {
	NSNumber* moduleId = [self registeredModuleIDForModule:module];
	if(moduleId == nil)
	{
		@throw [NSException exceptionWithName:NSInvalidArgumentException reason:@"Module has not been registered" userInfo:nil];
	}
	[self removeAllListenersFromModuleWithID:moduleId.intValue forEvent:event];
}

-(void)removeAllListenersFromModuleWithID:(int)moduleId forEvent:(NSString*)event {
	[[self moduleEventsForID:moduleId] removeAllListenersForEvent:event];
}

-(void)removeAllListenersFromModule:(id<RNEventConformer>)module {
	NSNumber* moduleId = [self registeredModuleIDForModule:module];
	if(moduleId == nil)
	{
		@throw [NSException exceptionWithName:NSInvalidArgumentException reason:@"Module has not been registered" userInfo:nil];
	}
	[self removeAllListenersFromModuleWithID:moduleId.intValue];
}

-(void)removeAllListenersFromModuleWithID:(int)moduleId {
	[[self moduleEventsForID:moduleId] removeAllListeners];
}

@end
