package com.lufinkey.react.eventemitter;

import com.facebook.react.bridge.Callback;

public class RNEventCallback
{
	private int callbackId;
	private Callback callback;
	private boolean onlyOnce;

	public RNEventCallback(int callbackId, Callback callback, boolean onlyOnce)
	{
		this.callbackId = callbackId;
		this.callback = callback;
		this.onlyOnce = onlyOnce;
	}

	public int getID()
	{
		return callbackId;
	}

	public Callback getCallback()
	{
		return callback;
	}

	public boolean isCalledOnlyOnce()
	{
		return onlyOnce;
	}
}
