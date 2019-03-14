package com.lufinkey.react.eventemitter;

public interface RNEventConformer {
	void __registerAsJSEventEmitter(int moduleId);

	void onNativeEvent(String eventName, Object... args);
	void onJSEvent(String eventName, Object... args);
	void onEvent(String eventName, Object... args);
}
