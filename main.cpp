#include <string>
#include <iostream>

#include "model/pins/OutputPin.h"
#include "model/messenger/InputMessenger.h"

using namespace std;

int main () {
	initializeCore();
	
	OutputPin* p1 = OutputPin::factory(100, digital);
	p1->digitalWrite(true);
	
	OutputPin* p2 = OutputPin::factory(101, digital);
	p2->digitalWrite(false);
	
	OutputPin* p3 = OutputPin::factory(102, digital);
	p3->digitalWrite(true);
	
	OutputPin* p4 = OutputPin::factory(103, digital);
	p4->digitalWrite(false);
	
	char path[] = "/tmp/pbOffApp.socket";
	InputMessenger msger((const char*) &path, [](string &input)->void{
		cout << input << endl;
	});
	if (msger.getErrorId())
		cout << "error: " << msger.getErrorId() << endl;
	else
		msger.runLoop();
}