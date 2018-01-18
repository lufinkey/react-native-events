
#import <Foundation/Foundation.h>

@interface RNModuleEvents : NSObject

-(void)addListener:(void(^)(NSArray*))listener forEvent:(NSString*)event onlyOnce:(BOOL)once;
-(void)prependListener:(void(^)(NSArray*))listener forEvent:(NSString*)event onlyOnce:(BOOL)once;
-(void)removeListener:(void(^)(NSArray*))listener forEvent:(NSString*)event;
-(void)removeAllListenersForEvent:(NSString*)event;
-(void)removeAllListeners;

-(NSUInteger)listenerCountForEvent:(NSString*)event;

-(BOOL)emitEvent:(NSString*)event withParams:(NSArray*)params;

@end
