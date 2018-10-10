
import {
	DeviceEventEmitter,
	NativeModules,
	Platform
} from 'react-native';
import EventEmitter from 'events';

const { RNEventEmitter } = NativeModules;



//========= INTERNAL FUNCTIONS =========//

const EVENT_NAME = "ayylmao_dicksnshit_nobodyUsethisevent PLS OK THANKS";

let moduleIdCounter = 1;
function getNewModuleId() {
	let moduleId = moduleIdCounter;
	moduleIdCounter++;
	return moduleId;
}

const registeredModules = {};

function sendJSEvents(moduleInfo, eventName, args) {
	for(const subscriber of moduleInfo.preSubscribers) {
		subscriber.emit(eventName, ...args);
	}
	if(moduleInfo.mainEmitter) {
		moduleInfo.mainEmitter.emit(eventName, ...args);
	}
	for(const subscriber of moduleInfo.subscribers) {
		subscriber.emit(eventName, ...args);
	}
}

// function called when an event is emitted from native code
function onNativeModuleEvent(event) {
	var moduleInfo = registeredModules[event.moduleId];
	if(moduleInfo == null) {
		console.error("Received event for unregistered module with id "+event.moduleId);
		return;
	}
	sendJSEvents(moduleInfo, event.eventName, event.args);
}

DeviceEventEmitter.addListener(EVENT_NAME, onNativeModuleEvent);





//========= EXPORTED FUNCTIONS =========//

const RNEvents = {};
module.exports = RNEvents;



RNEvents.register = (nativeModule) => {
	if(!nativeModule.__registerAsJSEventEmitter) {
		throw new Error("Native module does not conform to RNEventConformer");
	}
	else if(nativeModule.__rnEventsId != null) {
		throw new Error("Native module has already been registered");
	}

	// register native module
	const moduleId = getNewModuleId();
	nativeModule.__registerAsJSEventEmitter(moduleId);
	registeredModules[moduleId] = {
		nativeModule: nativeModule,
		mainEmitter: null,
		preSubscribers: [],
		subscribers: []
	};

	// define __rnEventsId property
	Object.defineProperty(nativeModule, '__rnEventsId', {
		value: moduleId,
		writable: false
	});

	return nativeModule;
}



RNEvents.conform = (nativeModule) => {
	if(!nativeModule.__registerAsJSEventEmitter) {
		throw new Error("Native module does not conform to RNEventConformer");
	}
	else if(nativeModule.__rnEventsId == null) {
		throw new Error("Native module has not been registered");
	}
	const moduleId = nativeModule.__rnEventsId;
	const moduleInfo = registeredModules[moduleId];
	if(moduleInfo == null) {
		throw new Error("No module info found for native module");
	}
	else if(moduleInfo.mainEmitter) {
		throw new Error("Native module has already been conformed");
	}

	// apply event emitter methods
	var eventEmitter = new EventEmitter();
	var emitterKeys = Object.keys(EventEmitter.prototype);
	for(var i=0; i<emitterKeys.length; i++) {
		var key = emitterKeys[i];
		var value = EventEmitter.prototype[key];

		if(typeof value == 'function' && key != 'emit') {
			Object.defineProperty(nativeModule, key, {
				value: value.bind(eventEmitter),
				writable: false
			});
		}
	}
	// set custom emit method
	nativeModule.emit = (eventName, ...args) => {
		// send native event
		RNEventEmitter.emit(moduleId, eventName, args);
		// send js events
		sendJSEvents(moduleInfo, eventName, args);
	};
	// update module info
	moduleInfo.mainEmitter = eventEmitter;

	return nativeModule;
}



RNEvents.emitNativeEvent = (nativeModule, eventName, ...args) => {
	if(!nativeModule.__registerAsJSEventEmitter) {
		throw new Error("Native module does not conform to RNEventConformer");
	}
	else if(nativeModule.__rnEventsId == null) {
		throw new Error("Native module has not been registered");
	}
	const moduleId = nativeModule.__rnEventsId;
	const moduleInfo = registeredModules[moduleId];
	if(moduleInfo == null) {
		throw new Error("No module info found for native module");
	}

	// send native event
	RNEventEmitter.emit(moduleId, eventName, args);
}



RNEvents.emitJSEvent = (nativeModule, eventName, ...args) => {
	if(!nativeModule.__registerAsJSEventEmitter) {
		throw new Error("Native module does not conform to RNEventConformer");
	}
	else if(nativeModule.__rnEventsId == null) {
		throw new Error("Native module has not been registered");
	}
	const moduleId = nativeModule.__rnEventsId;
	const moduleInfo = registeredModules[moduleId];
	if(moduleInfo == null) {
		throw new Error("No module info found for native module");
	}

	// send js events
	sendJSEvents(moduleInfo, eventName, args);
}



RNEvents.addSubscriber = (nativeModule, subscriber) => {
	if(!nativeModule.__registerAsJSEventEmitter) {
		throw new Error("Native module does not conform to RNEventConformer");
	}
	else if(nativeModule.__rnEventsId == null) {
		throw new Error("Native module has not been registered");
	}
	else if(!(subscriber instanceof EventEmitter)) {
		throw new Error("subscriber must be an EventEmitter");
	}
	const moduleId = nativeModule.__rnEventsId;
	const moduleInfo = registeredModules[moduleId];
	if(moduleInfo == null) {
		throw new Error("No module info found for native module");
	}

	// add subscriber
	moduleInfo.subscribers.push(subscriber);
}



RNEvents.removeSubscriber = (nativeModule, subscriber) => {
	if(!nativeModule.__registerAsJSEventEmitter) {
		throw new Error("Native module does not conform to RNEventConformer");
	}
	else if(nativeModule.__rnEventsId == null) {
		throw new Error("Native module has not been registered");
	}
	else if(!(subscriber instanceof EventEmitter)) {
		throw new Error("subscriber must be an EventEmitter");
	}
	const moduleId = nativeModule.__rnEventsId;
	const moduleInfo = registeredModules[moduleId];
	if(moduleInfo == null) {
		throw new Error("No module info found for native module");
	}

	// remove subscriber
	const index = moduleInfo.subscribers.indexOf(subscriber);
	if(index != -1) {
		moduleInfo.subscribers.splice(index, 1);
	}
}



RNEvents.addPreSubscriber = (nativeModule, subscriber) => {
	if(!nativeModule.__registerAsJSEventEmitter) {
		throw new Error("Native module does not conform to RNEventConformer");
	}
	else if(nativeModule.__rnEventsId == null) {
		throw new Error("Native module has not been registered");
	}
	else if(!(subscriber instanceof EventEmitter)) {
		throw new Error("subscriber must be an EventEmitter");
	}
	const moduleId = nativeModule.__rnEventsId;
	const moduleInfo = registeredModules[moduleId];
	if(moduleInfo == null) {
		throw new Error("No module info found for native module");
	}

	// add subscriber
	moduleInfo.preSubscribers.push(subscriber);
}



RNEvents.removePreSubscriber = (nativeModule, subscriber) => {
	if(!nativeModule.__registerAsJSEventEmitter) {
		throw new Error("Native module does not conform to RNEventConformer");
	}
	else if(nativeModule.__rnEventsId == null) {
		throw new Error("Native module has not been registered");
	}
	else if(!(subscriber instanceof EventEmitter)) {
		throw new Error("subscriber must be an EventEmitter");
	}
	const moduleId = nativeModule.__rnEventsId;
	const moduleInfo = registeredModules[moduleId];
	if(moduleInfo == null) {
		throw new Error("No module info found for native module");
	}

	// remove subscriber
	const index = moduleInfo.preSubscribers.indexOf(subscriber);
	if(index != -1) {
		moduleInfo.preSubscribers.splice(index, 1);
	}
}
