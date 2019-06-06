#include "Pin.h"

Pin::Pin (int pin): pbPin(pin) {
}

Pin::~Pin () {
}

bool Pin::isInitialized () {
	return initialized;
}

bool Pin::operator== (int other) {
	return pbPin == other;
}

bool Pin::operator!= (int other) {
	return pbPin != other;
}

int Pin::getPinNum () {
	return pbPin;
}