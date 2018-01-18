package com.lufinkey.react.eventemitter;

public interface RNEventConformer
{
	void __registerAsJSEventEmitter(int moduleId);

	void onJSEvent(String eventName, Object... args);
}
