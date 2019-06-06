#ifndef OUTPUT_PIN_H
#define OUTPUT_PIN_H

#include "Pin.h"

enum OutputType {
	digital,
	analog
};

enum LocationType {
	local,
	remote
};

class OutputPin: public Pin {
	private:
		void (*fSetMode)(int, OutputType) = nullptr;
		void (*fDigitalWrite)(int, bool) = nullptr;
		void (*fAnalogWrite)(int, int) = nullptr;
		const OutputType type;
		int currentState = 0;
	
	protected:
		void init ();
		void unInit ();
	
	public:
		OutputPin (int, OutputType, LocationType);
		~OutputPin();
		void digitalWrite(bool);
		void analogWrite(int);
		
		static OutputPin* factory (int, OutputType);
		
		friend void registerAsLocal (OutputPin*);
		friend void registerAsRemote (OutputPin*);
};

#endif