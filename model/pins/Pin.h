#ifndef PIN_H
#define PIN_H

void initializeCore ();

class Pin {
	protected:
		const int pbPin;
		bool initialized = false;
		virtual void init () = 0;
		virtual void unInit () = 0;
		
	public:
		Pin (int);
		~Pin ();
		bool isInitialized ();
		bool operator== (int);
		bool operator!= (int);
		int getPinNum ();
};

#endif