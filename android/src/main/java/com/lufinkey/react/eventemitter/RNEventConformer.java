package com.lufinkey.react.eventemitter;

import com.facebook.react.bridge.ReactMethod;

public interface RNEventConformer
{
	@ReactMethod
	void __registerAsJSEventEmitter(int moduleId);

	void onModuleEvent(String eventName, Object... args);
}
