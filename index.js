
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


function onNativeModuleEvent(event)
{
	var module = getRegisteredModule(event.moduleId);
	if(module == null)
	{
		return;
	}
	module._eventEmitter.emit(event.eventName, ...event.args);
}


DeviceEventEmitter.addListener(EVENT_NAME, onNativeModuleEvent);


function registerNativeModule(nativeModule)
{
	if(!nativeModule.__registerAsJSEventEmitter)
	{
		throw new Error("Native module does not conform to RNEventConformer");
	}
	else if(nativeModule._eventEmitter)
	{
		throw new Error("Native module has already been registered");
	}

	// register native module
	const moduleId = getNewModuleId();
	nativeModule.__registerAsJSEventEmitter(moduleId);
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
	nativeModule.emit = function(eventName, ...args)
	{
		RNEventEmitter.emit(eventName, args);
		eventEmitter.emit(eventName, ...args);
	}
	// set event emitter
	nativeModule._eventEmitter = eventEmitter;

	return nativeModule;
}


export default {
	registerNativeModule: registerNativeModule
};
