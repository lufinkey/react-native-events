package com.lufinkey.react.eventemitter;

import com.facebook.react.bridge.Callback;

import java.util.ArrayList;
import java.util.HashMap;

public class RNModuleEvents
{
	HashMap<String, ArrayList<RNEventCallback>> eventListeners;

	public void addEventListener(String eventName, int callbackId, Callback callback, boolean once)
	{
		ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
		if(listeners == null)
		{
			listeners = new ArrayList<>();
		}
		listeners.add(new RNEventCallback(callbackId, callback, once));
	}

	public void removeEventListener(String eventName, int callbackId)
	{
		ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
		for(int i=0; i<listeners.size(); i++)
		{
			RNEventCallback listener = listeners.get(i);
			if(listener.getID() == callbackId)
			{
				listeners.remove(i);
				return;
			}
		}
	}

	public boolean emit(String eventName, Object... args)
	{
		ArrayList<RNEventCallback> listeners = null;
		synchronized (eventListeners)
		{
			listeners = eventListeners.get(eventName);
		}
		synchronized (listeners)
		{
			if (listeners != null && listeners.size() > 0)
			{
				ArrayList<RNEventCallback> tmpListeners = new ArrayList<>(listeners);
				// remove "once" event listeners
				for (int i=0; i<listeners.size(); i++)
				{
					RNEventCallback listener = listeners.get(i);
					if(listener.isCalledOnlyOnce())
					{
						listeners.remove(i);
						i--;
					}
				}
				//invoke events
				for (int i = 0; i < tmpListeners.size(); i++)
				{
					RNEventCallback listener = tmpListeners.get(i);
					listener.getCallback().invoke(args);
				}
				return true;
			}
			return false;
		}
	}
}
