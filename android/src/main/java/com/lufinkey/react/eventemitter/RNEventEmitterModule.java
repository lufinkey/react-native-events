
package com.lufinkey.react.eventemitter;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import java.util.HashMap;

public class RNEventEmitterModule extends ReactContextBaseJavaModule
{
	private final ReactApplicationContext reactContext;

	public RNEventEmitterModule(ReactApplicationContext reactContext)
	{
		super(reactContext);
		this.reactContext = reactContext;
	}

	@Override
	public String getName()
	{
		return "RNEventEmitter";
	}

	@ReactMethod
	public void addEventListener(int moduleId, String event, int callbackId, Callback callback)
	{
		//TODO add event listener
	}
}
