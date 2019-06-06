#include "InputMessenger.h"

InputMessenger::InputMessenger (const char *addr, void(*inputParser)(std::string&)):
parseInput(inputParser) {
	// Remove socket, if last session not properly closed
	unlink(addr);
	
	// create local socket
	socketHandler = socket(AF_UNIX, SOCK_STREAM, 0);
	if (socketHandler == -1)
		errorId = errno;
	
	// create the socket name
	name.sun_family = AF_UNIX;
	// copy the 0 terminated address
	bool inZeroState = false;
	for (int i = 0; i < 108; i++) {
		if (inZeroState || !addr[i]) {
			inZeroState = true;
			name.sun_path[i] = 0;
		}
		else
			name.sun_path[i] = addr[i];
	}
	
	// bind socket and its name
	if (bind(socketHandler, (const struct sockaddr *) &name,
				sizeof(name)) == -1)
		errorId = errno;
	
	// mark socket for listening and a maximum backlog of 4
	if (listen(socketHandler, 4) == -1)
		errorId = errno;
}


InputMessenger::~InputMessenger () {
	close(socketHandler);
	unlink(name.sun_path);
}


void InputMessenger::runLoop () {
	while (true) {
		// wait for incoming connections
		int connectionHandle = accept(socketHandler, nullptr, 0);
		if (connectionHandle == -1) {
			errorId = errno;
			break;
		}
		
		// receive next data packet
		int readBytes;
		char buffer[BUFFER_SIZE];
		std::string returner = "";
		while ((readBytes = read(connectionHandle, &buffer, BUFFER_SIZE)) > 0) {
			returner.append(buffer, readBytes);
		}
		if (readBytes == -1)
			errorId = errno;
		else
			parseInput(returner);
		
		// exit loop, if quitting is wanted
		if (quitFlag)
			break;
	}
}


void InputMessenger::setQuitFlag (bool flag) {
	quitFlag = flag;
}


int InputMessenger::getErrorId () {
	return errorId;
}