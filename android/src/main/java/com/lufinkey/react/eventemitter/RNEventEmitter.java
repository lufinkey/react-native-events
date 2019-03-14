
package com.lufinkey.react.eventemitter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;

@ReactModule(name = "RNEventEmitter")
public class RNEventEmitter extends ReactContextBaseJavaModule {
	private final ReactApplicationContext reactContext;

	public static final String EVENT_NAME = "ayylmao_dicksnshit_nobodyUsethisevent PLS OK THANKS";

	private final HashMap<Integer, RNEventConformer> registeredModules = new HashMap<>();
	private final HashMap<Integer, RNModuleEvents> modules = new HashMap<>();

	public RNEventEmitter(ReactApplicationContext reactContext) {
		super(reactContext);
		this.reactContext = reactContext;
	}

	@Override
	public String getName() {
		return "RNEventEmitter";
	}



	public static RNEventEmitter getMainEventEmitter(ReactApplicationContext context) {
		return context.getNativeModule(RNEventEmitter.class);
	}



	public static void registerEventEmitterModule(ReactApplicationContext context, int moduleId, RNEventConformer module) {
		RNEventEmitter eventEmitter = getMainEventEmitter(context);
		if(eventEmitter == null) {
			System.out.println("Error: No RNEventEmitter is available to register to");
			return;
		}
		eventEmitter.registerModule(moduleId, module);
	}

	private void registerModule(int moduleId, RNEventConformer module) {
		synchronized (registeredModules) {
			if(registeredModules.containsValue(module)) {
				throw new IllegalArgumentException("Module "+module+" has already been registered");
			}
			if(registeredModules.containsKey(moduleId)) {
				throw new IllegalArgumentException("moduleId "+moduleId+" has already been registered to a module");
			}
			registeredModules.put(moduleId, module);
		}
	}



	private RNModuleEvents getModuleEvents(int moduleId) {
		synchronized (modules) {
			RNModuleEvents moduleEvents = modules.get(moduleId);
			if (moduleEvents == null) {
				moduleEvents = new RNModuleEvents();
				modules.put(moduleId, moduleEvents);
			}
			return moduleEvents;
		}
	}

	private RNEventConformer getRegisteredModule(int moduleId) {
		synchronized (registeredModules) {
			return registeredModules.get(moduleId);
		}
	}

	private Integer getRegisteredModuleID(RNEventConformer module) {
		synchronized (registeredModules) {
			for (HashMap.Entry<Integer, RNEventConformer> entry : registeredModules.entrySet()) {
				if(entry.getValue() == module) {
					return entry.getKey();
				}
			}
			return null;
		}
	}

	private WritableArray fromObjectArray(Object[] args) {
		WritableArray array = Arguments.createArray();
		for(Object arg : args) {
			if(arg == null) {
				array.pushNull();
			}
			else if(arg instanceof Boolean) {
				array.pushBoolean((Boolean)arg);
			}
			else if(arg instanceof Integer) {
				array.pushInt((Integer)arg);
			}
			else if(arg instanceof Double) {
				array.pushDouble((Double)arg);
			}
			else if(arg instanceof Float) {
				array.pushDouble((double)((Float)arg));
			}
			else if(arg instanceof String) {
				array.pushString((String)arg);
			}
			else if(arg instanceof WritableArray) {
				array.pushArray((WritableArray)arg);
			}
			else if(arg instanceof WritableMap) {
				array.pushMap((WritableMap)arg);
			}
			else {
				throw new IllegalArgumentException("Illegal object type");
			}
		}
		return array;
	}



	public static void emitEvent(ReactApplicationContext context, RNEventConformer module, String eventName, Object... args) {
		RNEventEmitter eventEmitter = getMainEventEmitter(context);
		if(eventEmitter == null) {
			System.out.println("Error: No RNEventEmitter is available to emit "+eventName+" event");
			return;
		}
		eventEmitter.emit(module, eventName, args);
	}

	public void emit(RNEventConformer module, String eventName, Object... args) {
		Integer moduleId = getRegisteredModuleID(module);
		if(moduleId == null) {
			System.out.println("Error: Cannot emit "+eventName+" event before "+module+" module has been registered");
			return;
		}
		getModuleEvents(moduleId).emit(eventName, args);

		WritableMap jsEvent = Arguments.createMap();
		jsEvent.putInt("moduleId", moduleId);
		jsEvent.putString("eventName", eventName);
		jsEvent.putArray("args", fromObjectArray(args));

		reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_NAME, jsEvent);

		module.onEvent(eventName, args);
		module.onNativeEvent(eventName, args);
	}

	@ReactMethod
	private void emit(int moduleId, String eventName, ReadableArray args) {
		RNEventConformer module = getRegisteredModule(moduleId);
		if(module == null) {
			throw new IllegalArgumentException("No module registered with ID "+moduleId);
		}

		Object[] argsArray = args.toArrayList().toArray();
		getModuleEvents(moduleId).emit(eventName, argsArray);

		module.onEvent(eventName, argsArray);
		module.onJSEvent(eventName, argsArray);
	}



	public void addListener(RNEventConformer module, String eventName, Callback callback, boolean once) {
		Integer moduleId = getRegisteredModuleID(module);
		if(moduleId == null) {
			throw new IllegalArgumentException("Module "+module+" has not been registered");
		}
		addListener(moduleId, eventName, callback, once);
	}

	public void addListener(RNEventConformer module, String eventName, Callback callback) {
		addListener(module, eventName, callback, false);
	}

	public void addListener(int moduleId, String eventName, Callback callback) {
		addListener(moduleId, eventName, callback, false);
	}

	public void addListener(int moduleId, String eventName, Callback callback, boolean once) {
		getModuleEvents(moduleId).addListener(eventName, callback, once);
	}



	public void prependListener(RNEventConformer module, String eventName, Callback callback, boolean once) {
		Integer moduleId = getRegisteredModuleID(module);
		if(moduleId == null) {
			throw new IllegalArgumentException("Module "+module+" has not been registered");
		}
		prependListener(moduleId, eventName, callback, once);
	}

	public void prependListener(RNEventConformer module, String eventName, Callback callback) {
		prependListener(module, eventName, callback, false);
	}

	public void prependListener(int moduleId, String eventName, Callback callback) {
		prependListener(moduleId, eventName, callback, false);
	}

	public void prependListener(int moduleId, String eventName, Callback callback, boolean once) {
		getModuleEvents(moduleId).prependListener(eventName, callback, once);
	}



	public void removeListener(RNEventConformer module, String eventName, Callback callback) {
		Integer moduleId = getRegisteredModuleID(module);
		if(moduleId == null) {
			throw new IllegalArgumentException("Module "+module+" has not been registered");
		}
		removeListener(moduleId, eventName, callback);
	}

	public void removeListener(int moduleId, String eventName, Callback callback) {
		getModuleEvents(moduleId).removeListener(eventName, callback);
	}



	public void removeAllListeners(RNEventConformer module, String eventName) {
		Integer moduleId = getRegisteredModuleID(module);
		if(moduleId == null) {
			throw new IllegalArgumentException("Module "+module+" has not been registered");
		}
		removeAllListeners(moduleId, eventName);
	}

	public void removeAllListeners(int moduleId, String eventName) {
		getModuleEvents(moduleId).removeAllListeners(eventName);
	}
}
