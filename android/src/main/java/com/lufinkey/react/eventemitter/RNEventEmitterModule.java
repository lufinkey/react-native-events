
package com.lufinkey.react.eventemitter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;

public class RNEventEmitterModule extends ReactContextBaseJavaModule
{
	private final ReactApplicationContext reactContext;

	public static final String EVENT_NAME = "ayylmao_dicksnshit_nobodyUsethisevent PLS OK THANKS";

	private final HashMap<RNEventConformer, Integer> registeredModules = new HashMap<>();
	private final HashMap<Integer, RNModuleEvents> modules = new HashMap<>();

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



	public static RNEventEmitterModule getMainEventEmitter(ReactApplicationContext context)
	{
		return context.getNativeModule(RNEventEmitterModule.class);
	}



	public static void registerEventEmitterModule(ReactApplicationContext context, int moduleId, RNEventConformer module)
	{
		RNEventEmitterModule eventEmitter = getMainEventEmitter(context);
		if(eventEmitter == null)
		{
			System.out.println("Error: No RNEventEmitterModule is available to register to");
			return;
		}
		eventEmitter.registerModule(moduleId, module);
	}

	private void registerModule(int moduleId, RNEventConformer module)
	{
		synchronized (registeredModules)
		{
			registeredModules.put(module, moduleId);
		}
	}



	private RNModuleEvents getModuleEvents(int moduleId)
	{
		synchronized (modules)
		{
			RNModuleEvents moduleEvents = modules.get(moduleId);
			if (moduleEvents == null)
			{
				moduleEvents = new RNModuleEvents();
				modules.put(moduleId, moduleEvents);
			}
			return moduleEvents;
		}
	}

	private RNEventConformer getRegisteredModule(int moduleId)
	{
		synchronized (registeredModules)
		{
			for (HashMap.Entry<RNEventConformer, Integer> entry : registeredModules.entrySet())
			{
				if(entry.getValue() == moduleId)
				{
					return entry.getKey();
				}
			}
			return null;
		}
	}

	private WritableArray fromObjectArray(Object[] args)
	{
		WritableArray array = Arguments.createArray();
		for(Object arg : args)
		{
			if(arg == null)
			{
				array.pushNull();
			}
			else if(arg instanceof Boolean)
			{
				array.pushBoolean((Boolean)arg);
			}
			else if(arg instanceof Integer)
			{
				array.pushInt((Integer)arg);
			}
			else if(arg instanceof Double)
			{
				array.pushDouble((Double)arg);
			}
			else if(arg instanceof Float)
			{
				array.pushDouble((double)((Float)arg));
			}
			else if(arg instanceof String)
			{
				array.pushString((String)arg);
			}
			else if(arg instanceof WritableArray)
			{
				array.pushArray((WritableArray)arg);
			}
			else if(arg instanceof WritableMap)
			{
				array.pushMap((WritableMap)arg);
			}
			else
			{
				throw new IllegalArgumentException("Illegal object type");
			}
		}
		return array;
	}



	public static void emitEvent(ReactApplicationContext context, RNEventConformer module, String eventName, Object... args)
	{
		RNEventEmitterModule eventEmitter = getMainEventEmitter(context);
		if(eventEmitter == null)
		{
			System.out.println("Error: No RNEventEmitterModule is available to emit "+eventName+" event");
			return;
		}
		eventEmitter.emit(module, eventName, args);
	}

	public void emit(RNEventConformer module, String eventName, Object... args)
	{
		Integer moduleId = null;
		synchronized (registeredModules)
		{
			moduleId = registeredModules.get(module);
		}
		if(moduleId == null)
		{
			System.out.println("Error: Cannot emit "+eventName+" event before "+module+" module has been registered");
			return;
		}
		getModuleEvents(moduleId).emit(eventName, args);

		WritableMap jsEvent = Arguments.createMap();
		jsEvent.putInt("moduleId", moduleId);
		jsEvent.putString("eventName", eventName);
		jsEvent.putArray("args", fromObjectArray(args));

		reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_NAME, jsEvent);
	}

	@ReactMethod
	private void emit(int moduleId, String eventName, ReadableArray args)
	{
		RNEventConformer module = getRegisteredModule(moduleId);
		if(module == null)
		{
			throw new IllegalArgumentException("No module registered with ID "+moduleId);
		}

		Object[] argsArray = args.toArrayList().toArray();
		getModuleEvents(moduleId).emit(eventName, argsArray);
		module.onJSEvent(eventName, argsArray);
	}



	public void addListener(RNEventConformer module, String eventName, Callback callback, boolean once)
	{
		Integer moduleId = registeredModules.get(module);
		if(moduleId == null)
		{
			throw new IllegalArgumentException("Module "+module+" has not been registered");
		}
		addListener(moduleId, eventName, callback, once);
	}

	public void addListener(RNEventConformer module, String eventName, Callback callback)
	{
		addListener(module, eventName, callback, false);
	}

	public void addListener(int moduleId, String eventName, Callback callback)
	{
		addListener(moduleId, eventName, callback, false);
	}

	public void addListener(int moduleId, String eventName, Callback callback, boolean once)
	{
		getModuleEvents(moduleId).addListener(eventName, callback, once);
	}



	public void prependListener(RNEventConformer module, String eventName, Callback callback, boolean once)
	{
		Integer moduleId = registeredModules.get(module);
		if(moduleId == null)
		{
			throw new IllegalArgumentException("Module "+module+" has not been registered");
		}
		prependListener(moduleId, eventName, callback, once);
	}

	public void prependListener(RNEventConformer module, String eventName, Callback callback)
	{
		prependListener(module, eventName, callback, false);
	}

	public void prependListener(int moduleId, String eventName, Callback callback)
	{
		prependListener(moduleId, eventName, callback, false);
	}

	public void prependListener(int moduleId, String eventName, Callback callback, boolean once)
	{
		getModuleEvents(moduleId).prependListener(eventName, callback, once);
	}



	public void removeListener(RNEventConformer module, String eventName, Callback callback)
	{
		Integer moduleId = registeredModules.get(module);
		if(moduleId == null)
		{
			throw new IllegalArgumentException("Module "+module+" has not been registered");
		}
		removeListener(moduleId, eventName, callback);
	}

	public void removeListener(int moduleId, String eventName, Callback callback)
	{
		getModuleEvents(moduleId).removeListener(eventName, callback);
	}



	public void removeAllListeners(RNEventConformer module, String eventName)
	{
		Integer moduleId = registeredModules.get(module);
		if(moduleId == null)
		{
			throw new IllegalArgumentException("Module "+module+" has not been registered");
		}
		removeAllListeners(moduleId, eventName);
	}

	public void removeAllListeners(int moduleId, String eventName)
	{
		getModuleEvents(moduleId).removeAllListeners(eventName);
	}
}
