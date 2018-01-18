
#import <Foundation/Foundation.h>

@protocol RNEventConformer <NSObject>

-(void)__registerAsJSEventEmitter:(int)moduleId;

@optional
-(void)onNativeEvent:(NSString*)eventName params:(NSArray*)params;
-(void)onJSEvent:(NSString*)eventName params:(NSArray*)params;
-(void)onEvent:(NSString*)eventName params:(NSArray*)params;

@end
