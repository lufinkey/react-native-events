package com.lufinkey.react.eventemitter;

import com.facebook.react.bridge.Callback;

import java.util.ArrayList;
import java.util.HashMap;

public class RNModuleEvents
{
	HashMap<String, ArrayList<RNEventCallback>> eventListeners;

	public void addListener(String eventName, int callbackId, Callback callback, boolean once)
	{
		synchronized (eventListeners)
		{
			ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
			if (listeners == null)
			{
				listeners = new ArrayList<>();
				eventListeners.put(eventName, listeners);
			}
			listeners.add(new RNEventCallback(callbackId, callback, once));
		}
	}

	public void prependListener(String eventName, int callbackId, Callback callback, boolean once)
	{
		synchronized (eventListeners)
		{
			ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
			if (listeners == null)
			{
				listeners = new ArrayList<>();
				eventListeners.put(eventName, listeners);
			}
			listeners.add(0, new RNEventCallback(callbackId, callback, once));
		}
	}

	public void removeListener(String eventName, int callbackId)
	{
		synchronized (eventListeners)
		{
			ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
			for (int i=0; i<listeners.size(); i++)
			{
				RNEventCallback listener = listeners.get(i);
				if (listener.getID() == callbackId)
				{
					listeners.remove(i);
					return;
				}
			}
		}
	}

	public void removeAllListeners(String eventName)
	{
		synchronized (eventListeners)
		{
			if (eventName == null)
			{
				eventListeners.clear();
			}
			else
			{
				eventListeners.remove(eventName);
			}
		}
	}

	public int getListenerCount(String eventName)
	{
		synchronized (eventListeners)
		{
			ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
			if (listeners == null)
			{
				return 0;
			}
			return listeners.size();
		}
	}

	public boolean emit(String eventName, Object... args)
	{
		ArrayList<RNEventCallback> tmpListeners = null;

		synchronized (eventListeners)
		{
			ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
			tmpListeners = new ArrayList<>(listeners);

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
		}

		if (tmpListeners != null && tmpListeners.size() > 0)
		{
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
