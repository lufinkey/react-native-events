
# react-native-events

A full EventEmitter implementation for react-native modules

The *documented* way to send and receive events from a native module requires you to use different native classes *and* different javascript classes on iOS vs Android, *and* the events are global and could easily collide with another module's events. This module unifies the native events process by allowing native modules to conform to node's [EventEmitter](https://nodejs.org/dist/latest-v9.x/docs/api/events.html#events_class_eventemitter) class, meaning all of the methods from EventEmitter are callable from your module instance.




## Setup

Since this module is only meant to be used with other native modules, you have to add this module as a dependency inside of your native module (NOT inside of your main project):

```bash
npm install --save react-native-events
```

**note:** Inside your main project (NOT inside your native module), after setting up your native module and adding it to your `package.json` file, you must run `npm install` to install your module and its dependencies, and `react-native link` to actually link the native code to your app project.

In order to set up your native module to conform to an EventEmitter on each platform, you must perform the following steps:

#### iOS

Add `$(SRCROOT)/../../react-native-events/ios` to *Header Search Paths* in the project settings of your native module. (If your module is a scoped package, you'll need to add one more `../` to the path)

Then make your native module conform to `RNEventConformer`:

```objc
// MyNativeModule.h

#import <React/RCTBridgeModule.h>

#import <RNEventEmitter/RNEventEmitter.h>

@interface MyNativeModule : NSObject <RCTBridgeModule, RNEventConformer>

...

@end
```

```objc
// MyNativeModule.m

#import "MyNativeModule.h"

@implementation MyNativeModule

@synthesize bridge = _bridge;

...

RCT_EXPORT_METHOD(__registerAsJSEventEmitter:(int)moduleId)
{
	[RNEventEmitter registerEventEmitterModule:self withID:moduleId bridge:_bridge];
}

-(void)onNativeEvent:(NSString*)eventName params:(NSArray*)params
{
	// Called when an event for this module is emitted from native code
}

-(void)onJSEvent:(NSString*)eventName params:(NSArray*)params
{
	// Called when an event for this module is emitted from javascript
}

-(void)onEvent:(NSString*)eventName params:(NSArray*)params
{
	// Called when any event for this module is emitted
}

...

@end

```

#### Android

Edit `android/build.gradle` and add the `react-native-events` project to `dependencies`

```
...
dependencies {
	compile 'com.facebook.react:react-native:+'
	compile project(path: ':react-native-events')
}
...
```

Then make your native module conform to `RNEventConformer`:

```java
package com.reactlibrary.mynativemodule;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.lufinkey.react.eventemitter.RNEventConformer;
import com.lufinkey.react.eventemitter.RNEventEmitter;

public class MyNativeModule extends ReactContextBaseJavaModule implements RNEventConformer
{
	...
	
	@Override
	@ReactMethod
	public void __registerAsJSEventEmitter(int moduleId)
	{
		RNEventEmitter.registerEventEmitterModule(this.reactContext, moduleId, this);
	}
	
	@Override
	public void onNativeEvent(String eventName, Object... args)
	{
		// Called when an event for this module is emitted from native code
	}

	@Override
	public void onJSEvent(String eventName, Object... args)
	{
		// Called when an event for this module is emitted from javascript
	}

	@Override
	public void onEvent(String eventName, Object... args)
	{
		// Called when any event for this module is emitted
	}
	
	...
}
```




## Usage

In order for your native module to conform to node's EventEmitter class, you must first register your module as an event emitter inside your native module's `index.js` using the `register` method, and then conform it using the `conform` method:

```javascript
import { NativeModules } from 'react-native';
import RNEvents from 'react-native-events';

var MyNativeModule = NativeModules.MyNativeModule;

// register your event emitter to be able to send events
RNEvents.register(MyNativeModule);
// conform your native module to EventEmitter
RNEvents.conform(MyNativeModule);


export default MyNativeModule;
```

#### Sending and receiving events in javascript

Once your native module has been registered and conformed, you may use any method available in the [EventEmitter](https://nodejs.org/dist/latest-v9.x/docs/api/events.html#events_class_eventemitter) class.

To receive events, you can just use the `addListener` method from EventEmitter. To send events, you can just use the `emit` method from EventEmitter.

```javascript
import MyNativeModule from 'my-native-module-package';

// add a listener to listen for the "somethingHappened" event
MyNativeModule.addListener("somethingHappened", (arg1, arg2) => {
	console.log("my event was called, and I received some data!");
	console.log("arg1: ", arg1);
	console.log("arg2: ", arg2);
});

// send the "somethingHappened" event with 2 arguments
MyNativeModule.emit("somethingHappened", {a:"ayy", b:"lmao"}, {c:"It's 4:20 somewhere"});
```

#### Sending and receiving events in native code

Any event sent from your native code will be received in your native modules's javascript event listeners for that event. Likewise, any event sent from javascript will be received in the `onEvent` and `onJSEvent` methods in your native code.

The following code shows how to send the same `somethingHappened` event from your native code's module class:

**Objective-C**

```objc

// create event arguments
NSDictionary* arg1 = @{ @"a":@"ayy", @"b":@"lmao" };
NSDictionary* arg2 = @{ @"c":@"It's 4:20 somewhere" };

// send the event
[RNEventEmitter emitEvent:@"somethingHappened" withParams:@[ arg1, arg2 ] module:self bridge:_bridge];
```

**Java**

```objc

// create event arguments
WritableMap arg1 = Arguments.createMap();
data.putString("a", "ayy");
data.putString("b", "lmao");
WritableMap arg2 = Arguments.createMap();
data.putString("c", "It's 4:20 somewhere");

// send the events
RNEventEmitter.emitEvent(this.reactContext, this, "somethingHappened", arg1, arg2);
```


---


## API Reference

```javascript
import RNEvents from 'react-native-events';
```

### Methods

- **register**( *nativeModule* )

	Registers a native module to be able to send / receive events between native code and javascript.
	
	- *Parameters*
	
		- **nativeModule** - a react native module
	
	- *Returns*
	
		- The registered native module


- **conform**( *nativeModule* )

	Conforms a registered native module to the EventEmitter prototype. This creates an EventEmitter instance and adds all of its methods to the native module object. The native module will not inherit from EventEmitter and `nativeModule instanceof EventEmitter` will still return false. Calling the `emit` method will call `emitNativeEvent` and `emitJSEvent` on the module, in that order
	
	- *Parameters*
	
		- **nativeModule** - a react native module
	
	- *Returns*
	
		- The conformed native module


- **emitNativeEvent**( *nativeModule*, *eventName*, ...*args* )

	Sends an event to the module's native code. This will trigger the native module's `onEvent` and `onJSEvent` methods, in that order. Note that this only triggers the native code events. No javascript events will be emitted.
	
	- *Parameters*
	
		- **nativeModule** - a react native module
		- **eventName** - the name of the event
		- **args** - the arguments to be passed to the event


- **emitJSEvent**( *nativeModule*, *eventName*, ...*args* )

	Sends an event to the module's javascript event subscribers. This will trigger on EventEmitters subscribed with `addPreSubscriber`, on the conformed module, and on EventEmitters subscribed with `addSubscriber`, in that order. Note that this only triggers the javascript events. No native code events will be emitted.
	
	- *Parameters*
	
		- **nativeModule** - a react native module
		- **eventName** - the name of the event
		- **args** - the arguments to be passed to the event


- **addSubscriber**( *nativeModule*, *subscriber* )

	Subscribes an EventEmitter to the events sent by the native module.
	
	- *Parameters*
	
		- **nativeModule** - a react native module
		- **subscriber** - an EventEmitter to be subscribed to the module's events


- **removeSubscriber**( *nativeModule*, *subscriber* )

	Unsubscribes an EventEmitter from the events sent by the native module.
	
	- *Parameters*
	
		- **nativeModule** - a react native module
		- **subscriber** - an EventEmitter to be unsubscribed from the module's events


- **addPreSubscriber**( *nativeModule*, *subscriber* )

	Subscribes an EventEmitter to the events sent by the native module. Subscribers added using this method receive events before the conformed native module and before subscribers added using `addSubscriber`.
	
	- *Parameters*
	
		- **nativeModule** - a react native module
		- **subscriber** - an EventEmitter to be subscribed to the module's events


- **removePreSubscriber**( *nativeModule*, *subscriber* )

	Unsubscribes an EventEmitter from the events sent by the native module. This method will only unsubscribe subscribers added using the `addPreSubscriber` method.
	
	- *Parameters*
	
		- **nativeModule** - a react native module
		- **subscriber** - an EventEmitter to be unsubscribed from the module's events
