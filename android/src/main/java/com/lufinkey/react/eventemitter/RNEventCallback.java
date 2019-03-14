package com.lufinkey.react.eventemitter;

import com.facebook.react.bridge.Callback;

public class RNEventCallback {
	private Callback callback;
	private boolean onlyOnce;

	public RNEventCallback(Callback callback, boolean onlyOnce) {
		this.callback = callback;
		this.onlyOnce = onlyOnce;
	}

	public Callback getCallback() {
		return callback;
	}

	public boolean isCalledOnlyOnce() {
		return onlyOnce;
	}
}
