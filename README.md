
# react-native-event-emitter

A *non*-shitty (relatively speaking) EventEmitter implementation for react-native modules

If you're tired of how react-native claims to be a cross platform framework, yet still contains countless fragmented pieces of implementation, this is the repo for you. The *documented* way to send events from a native module requires you to use different native *and* javascript classes to implement events on iOS vs Android. This module unifies the native events process, *and* allows native modules to conform to node's EventEmitter.

## Setup

Since this module is only meant to be used with other native modules, you have to add this module as a dependency inside of your native module (NOT inside of your main project):

```bash
npm install --save https://github.com/lufinkey/react-native-event-emitter
```

**NOTE:** Inside your main project (NOT your native module), after setting up your native module and adding it to your `package.json` file, you must run `npm install` to install your module and its dependencies, and `react-native link` to actually link the native code to your app project.

Next, you must set up your native module to conform to an event emitter on each platform:

#### iOS

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

Then you must make your native module conform to `RNEventConformer` like so:

```java
package com.reactlibrary.mynativemodule;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.lufinkey.react.eventemitter.RNEventConformer;
import com.lufinkey.react.eventemitter.RNEventEmitter;

public class MyNativeModule extends ReactContextBaseJavaModule implements RNEventConformer
{
	...
	
	// Add the following methods to the body of your native module class
	
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
