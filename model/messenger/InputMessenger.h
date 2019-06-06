#ifndef INPUT_MESSENGER_H
#define INPUT_MESSENGER_H

#include <string>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>

#define BUFFER_SIZE 255

class InputMessenger {
	private:
		sockaddr_un name;
		int socketHandler;
		int errorId = 0;
		bool quitFlag = false;
		void (*parseInput) (std::string&);
	
	public:
		// Char Array must be 0 terminated and max 108 long
		InputMessenger (const char *, void(*)(std::string&));
		~InputMessenger ();
		void runLoop ();
		void setQuitFlag (bool);
		int getErrorId ();
};

#endif