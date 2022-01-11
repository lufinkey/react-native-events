
#import <React/RCTBridgeModule.h>

#import "RNEventConformer.h"
#import "RNEventBridge.h"

@interface RNEventEmitter : NSObject <RCTBridgeModule>

+(RNEventEmitter*)eventEmitterForBridge:(RCTBridge*)bridge;
+(void)registerEventEmitterModule:(id<RNEventConformer>)module withID:(int)moduleId bridge:(RCTBridge*)bridge;
+(void)emitEvent:(NSString*)event withParams:(NSArray*)params module:(id<RNEventConformer>)module bridge:(RCTBridge*)bridge;

-(void)registerModule:(id<RNEventConformer>)module withID:(int)moduleId;
-(id<RNEventConformer>)registeredModuleForID:(int)moduleId;

-(void)emitEvent:(NSString*)event withParams:(NSArray*)params forModule:(id<RNEventConformer>)module;

-(void)addListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModule:(id<RNEventConformer>)module runOnce:(BOOL)once;
-(void)addListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModule:(id<RNEventConformer>)module;
-(void)addListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModuleWithID:(int)moduleId runOnce:(BOOL)once;
-(void)addListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModuleWithID:(int)moduleId;

-(void)prependListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModule:(id<RNEventConformer>)module runOnce:(BOOL)once;
-(void)prependListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModule:(id<RNEventConformer>)module;
-(void)prependListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModuleWithID:(int) moduleId runOnce:(BOOL)once;
-(void)prependListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModuleWithID:(int)moduleId;

-(void)removeListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModule:(id<RNEventConformer>)module;
-(void)removeListener:(void(^)(NSArray*))listener forEvent:(NSString*)event fromModuleWithID:(int)moduleId;

-(void)removeAllListenersFromModule:(id<RNEventConformer>)module forEvent:(NSString*)event;
-(void)removeAllListenersFromModuleWithID:(int)moduleId forEvent:(NSString*)event;
-(void)removeAllListenersFromModule:(id<RNEventConformer>)module;
-(void)removeAllListenersFromModuleWithID:(int)moduleId;

@end
