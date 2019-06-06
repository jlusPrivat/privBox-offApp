#include <wiringPi.h>

#include "OutputPin.h"
#include "InputPin.h"

void initializeCore () {
	wiringPiSetup();
}

namespace {
	void lSetMode (int num, OutputType type) {
		pinMode(num, OUTPUT);
	}
	void lDigitalWrite (int num, bool state) {
		digitalWrite(num, state ? HIGH : LOW);
	}
	void lAnalogWrite (int num, int state) {
		analogWrite(num, state);
	}
}

void registerAsLocal (OutputPin *pin) {
	pin->fSetMode = &lSetMode;
	pin->fDigitalWrite = &lDigitalWrite;
	pin->fAnalogWrite = &lAnalogWrite;
}

void registerAsRemote (OutputPin *pin) {
	
}

OutputPin* OutputPin::factory (int pin, OutputType outT) {
	int p;
	LocationType t = local;
	switch (pin) {
		case 100:
			p = 0;
			break;
		case 101:
			p = 7;
			break;
		case 102:
			p = 9;
			break;
		case 103:
			p = 8;
			break;
		default:
			p = pin;
			t = remote;
	}
	return new OutputPin(p, outT, t);
}