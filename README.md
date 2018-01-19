
# react-native-event-emitter

A *non*-shitty (relatively speaking) EventEmitter](https://nodejs.org/dist/latest-v9.x/docs/api/events.html#events_class_eventemitter) implementation for react-native modules

If you're tired of how react-native claims to be a cross platform framework, yet still contains countless fragmented pieces of implementation, this is the repo for you. The *documented* way to send events from a native module requires you to use different native *and* javascript classes to implement events on iOS vs Android. This module unifies the native events process, *and* allows native modules to conform to node's EventEmitter](https://nodejs.org/dist/latest-v9.x/docs/api/events.html#events_class_eventemitter).

## Setup

Since this module is only meant to be used with other native modules, you have to add this module as a dependency inside of your native module (NOT inside of your main project):

```bash
npm install --save https://github.com/lufinkey/react-native-event-emitter
```

**NOTE:** Inside your main project (NOT your native module), after setting up your native module and adding it to your `package.json` file, you must run `npm install` to install your module and its dependencies, and `react-native link` to actually link the native code to your app project.

In order to set up your native module to conform to an EventEmitter on each platform, you must perform the following steps:

#### iOS

Add `$(SRCROOT)/../../react-native-event-emitter/ios` to *Header Search Paths* in your project settings. (If your project is a scoped package, you may need to add more `../` to the path)

Then make your native module conform to `RNEventConformer` like so:

```Objective-C
// MyNativeModule.h

#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#if __has_include("RNEventEmitter.h")
#import "RNEventEmitter.h"
#else
#import <RNEventEmitter/RNEventEmitter.h>
#endif

@interface MyNativeModule : NSObject <RCTBridgeModule, RNEventConformer>

...

@end
```

```
// MyNativeModule.m

#import "MyNativeModule.h"

@implementation MyNativeModule

@synthesize bridge = _bridge;

...

RCT_EXPORT_METHOD(__registerAsJSEventEmitter:(int)moduleId)
{
	[RNEventEmitter registerEventEmitterModule:self withID:moduleId bridge:_bridge];
}

...

@end

```

#### Android

Edit `android/build.gradle` and add the `react-native-event-emitter` project to `dependencies`

```
...
dependencies {
	compile 'com.facebook.react:react-native:+'
	compile project(path: ':react-native-event-emitter')
}
...
```

Then make your native module conform to `RNEventConformer` like so:

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
		//Called when an event for this module is emitted from native code
	}

	@Override
	public void onJSEvent(String eventName, Object... args)
	{
		//Called when an event for this module is emitted from javascript
	}

	@Override
	public void onEvent(String eventName, Object... args)
	{
		//Called when any event for this module is emitted
	}
	
	...
}
```

## Usage

If you would want your native module to conform to node's EventEmitter class, you must register your module as an event emitter inside your module's `index.js` file using the `registerNativeModule` method:

```javascript
import { NativeModules } from 'react-native';
import NativeModuleEvents from 'react-native-event-emitter';

const MyNativeModule = NativeModules.MyNativeModule;

// Add EventEmitter methods to your native module
MyNativeModule = NativeModuleEvents.registerNativeModule(MyNativeModule);

export default MyNativeModule;
```

#### Sending and receiving events in javascript

Once your native module has been registered with `registerNativeModule`, you may use any method available in the [EventEmitter](https://nodejs.org/dist/latest-v9.x/docs/api/events.html#events_class_eventemitter) class.

To receive events, you can just use the `addListener` method from EventEmitter:

```javascript

import MyNativeModule from 'my-native-module-package';

MyNativeModule.addListener("something-happened", (data) => {

});

```