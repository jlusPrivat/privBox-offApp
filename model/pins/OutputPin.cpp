#include "OutputPin.h"

OutputPin::OutputPin (int pin, OutputType outT, LocationType locT):
Pin(pin), type(outT) {
	switch (locT) {
		case local:
			registerAsLocal(this);
			break;
		default:
			registerAsRemote(this);
	}
	init();
}

OutputPin::~OutputPin () {
	unInit();
}

void OutputPin::init () {
	initialized = true;
	fSetMode(pbPin, type);
	digitalWrite(false);
}

void OutputPin::unInit () {
	initialized = false;
	digitalWrite(false);
}

void OutputPin::digitalWrite (bool state) {
	if (!initialized) return;
	switch (type) {
		case digital:
			fDigitalWrite(pbPin, state);
			currentState = state ? 1024 : 0;
			break;
		case analog:
			analogWrite(state ? 1024 : 0);
			break;
	}
}

void OutputPin::analogWrite (int state) {
	if (!initialized) return;
	switch (type) {
		case digital:
			digitalWrite(state >= 512);
			break;
		case analog:
			fAnalogWrite(pbPin, state);
			currentState = state;
			break;
	}
}