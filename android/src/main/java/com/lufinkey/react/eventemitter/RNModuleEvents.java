package com.lufinkey.react.eventemitter;

import com.facebook.react.bridge.Callback;

import java.util.ArrayList;
import java.util.HashMap;

public class RNModuleEvents {
	HashMap<String, ArrayList<RNEventCallback>> eventListeners = new HashMap<>();

	public void addListener(String eventName, Callback callback, boolean once) {
		synchronized (eventListeners) {
			ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
			if (listeners == null) {
				listeners = new ArrayList<>();
				eventListeners.put(eventName, listeners);
			}
			listeners.add(new RNEventCallback(callback, once));
		}
	}

	public void prependListener(String eventName, Callback callback, boolean once) {
		synchronized (eventListeners) {
			ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
			if (listeners == null) {
				listeners = new ArrayList<>();
				eventListeners.put(eventName, listeners);
			}
			listeners.add(0, new RNEventCallback(callback, once));
		}
	}

	public void removeListener(String eventName, Callback callback) {
		synchronized (eventListeners) {
			ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
			for (int i=0; i<listeners.size(); i++) {
				RNEventCallback listener = listeners.get(i);
				if (listener.getCallback() == callback) {
					listeners.remove(i);
					return;
				}
			}
		}
	}

	public void removeAllListeners(String eventName) {
		synchronized (eventListeners) {
			if (eventName == null) {
				eventListeners.clear();
			}
			else {
				eventListeners.remove(eventName);
			}
		}
	}

	public int getListenerCount(String eventName) {
		synchronized (eventListeners) {
			ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
			if (listeners == null) {
				return 0;
			}
			return listeners.size();
		}
	}

	public boolean emit(String eventName, Object... args) {
		ArrayList<RNEventCallback> tmpListeners = null;

		synchronized (eventListeners) {
			ArrayList<RNEventCallback> listeners = eventListeners.get(eventName);
			if(listeners != null) {
				tmpListeners = new ArrayList<>(listeners);
			}

			// remove "once" event listeners
			if(listeners != null) {
				for (int i = 0; i < listeners.size(); i++) {
					RNEventCallback listener = listeners.get(i);
					if (listener.isCalledOnlyOnce()) {
						listeners.remove(i);
						i--;
					}
				}
			}
		}

		if (tmpListeners != null && tmpListeners.size() > 0) {
			//invoke events
			for (int i = 0; i < tmpListeners.size(); i++) {
				RNEventCallback listener = tmpListeners.get(i);
				listener.getCallback().invoke(args);
			}
			return true;
		}
		return false;
	}
}
