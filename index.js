
import {
	DeviceEventEmitter,
	NativeModules,
	Platform
} from 'react-native';
import EventEmitter from 'events';

const { RNEventEmitter } = NativeModules;


const EVENT_NAME = "ayylmao_dicksnshit_nobodyUsethisevent PLS OK THANKS";


const moduleIdCounter = 0;
function getNewModuleId()
{
	const moduleId = moduleIdCounter;
	moduleIdCounter++;
	return moduleId;
}

const registeredModules = {};
function getRegisteredModule(moduleId)
{
	return registeredModules[''+moduleId];
}


function sendJSEvents(nativeModule, eventName, args)
{
	if(nativeModule.__rnEvents.preSubscribers)
	{
		for(const subscriber of nativeModule.__rnEvents.preSubscribers)
		{
			subscriber.emit(eventName, ...args);
		}
	}
	nativeModule.__rnEvents.mainEmitter.emit(eventName, ...args);
	if(nativeModule.__rnEvents.subscribers)
	{
		for(const subscriber of nativeModule.__rnEvents.subscribers)
		{
			subscriber.emit(eventName, ...args);
		}
	}
}

function onNativeModuleEvent(event)
{
	var nativeModule = getRegisteredModule(event.moduleId);
	if(nativeModule == null)
	{
		return;
	}
	console.log("event: ", event);
	sendJSEvents(nativeModule, event.eventName, event.args);
}


DeviceEventEmitter.addListener(EVENT_NAME, onNativeModuleEvent);


function registerNativeModule(nativeModule, options)
{
	if(!nativeModule.__registerAsJSEventEmitter)
	{
		throw new Error("Native module does not conform to RNEventConformer");
	}
	else if(nativeModule.__rnEvents)
	{
		throw new Error("Native module has already been registered");
	}

	// validate options
	options = Object.assign({}, options);
	if(options.preSubscribers != null && !(options.preSubscribers instanceof Array))
	{
		throw new Error("options.preSubscribers must be an array");
	}
	if(options.subscribers != null && !(options.subscribers instanceof Array))
	{
		throw new Error("options.subscribers must be an array");
	}

	// register native module
	let moduleId = getNewModuleId();
	nativeModule.__registerAsJSEventEmitter(moduleId);
	nativeModule.__rnEvents = {
		preSubscribers: options.preSubscribers,
		mainEmitter: null,
		subscribers: options.subscribers
	};
	registeredModules[''+moduleId] = nativeModule;

	// add EventEmitter functions to native module
	var eventEmitter = new EventEmitter();
	var emitterKeys = Object.keys(EventEmitter.prototype);
	for(var i=0; i<emitterKeys.length; i++)
	{
		var key = emitterKeys[i];
		var value = EventEmitter.prototype[key];

		if(typeof value == 'function')
		{
			nativeModule[key] = value.bind(eventEmitter);
		}
	}
	// set custom emit method
	nativeModule.emit = (eventName, ...args) => {
		// send native event
		RNEventEmitter.emit(moduleId, eventName, args);
		// send js events
		sendJSEvents(nativeModule, eventName, args);
	}
	// set event emitter
	nativeModule.__rnEvents.mainEmitter = eventEmitter;

	return nativeModule;
}


export default {
	registerNativeModule: registerNativeModule
};
