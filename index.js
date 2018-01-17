
import { NativeModules } from 'react-native';

const { RNEventEmitter } = NativeModules;

const moduleIdCounter = 0;

function registerNativeModule(nativeModule)
{
	if(!nativeModule.__registerAsJSEventEmitter)
	{
		throw new Error("Native module does not conform to RNEventConformer");
	}
	var moduleId = moduleIdCounter;
	moduleIdCounter++;
	nativeModule.__registerAsJSEventEmitter(moduleId);
}

export default {
	registerNativeModule: registerNativeModule
};
