
package com.lufinkey.react.eventemitter;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableArray;

import java.util.HashMap;
import java.util.Map;

public class RNEventEmitterModule extends ReactContextBaseJavaModule
{
	private final ReactApplicationContext reactContext;

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

	@ReactMethod
	public void addListener(int moduleId, String eventName, int callbackId, Callback callback, boolean once)
	{
		getModuleEvents(moduleId).addListener(eventName, callbackId, callback, once);
	}

	@ReactMethod
	public void prependListener(int moduleId, String eventName, int callbackId, Callback callback, boolean once)
	{
		getModuleEvents(moduleId).prependListener(eventName, callbackId, callback, once);
	}

	@ReactMethod
	public void removeListener(int moduleId, String eventName, int callbackId)
	{
		getModuleEvents(moduleId).removeListener(eventName, callbackId);
	}

	@ReactMethod
	public void removeAllListeners(int moduleId, String eventName)
	{
		getModuleEvents(moduleId).removeAllListeners(eventName);
	}

	@ReactMethod
	public void emit(int moduleId, String eventName, ReadableArray args, boolean callModuleEvent)
	{
		RNEventConformer module = getRegisteredModule(moduleId);
		if(module == null)
		{
			System.out.println("Error: no module with moduleId "+moduleId+" has been registered to emit events");
			return;
		}

		Object[] argsArray = args.toArrayList().toArray();
		getModuleEvents(moduleId).emit(eventName, argsArray);

		if(module != null && callModuleEvent)
		{
			module.onModuleEvent(eventName, argsArray);
		}
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
	}

	private void registerModule(int moduleId, RNEventConformer module)
	{
		synchronized (registeredModules)
		{
			registeredModules.put(module, moduleId);
		}
	}

	private static RNEventEmitterModule getMainEventEmitter(ReactApplicationContext context)
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
}
