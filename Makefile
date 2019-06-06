EXECUTABLE=pbOffApp

CC=g++
CFLAGS=-Wall
SOURCES := $(shell find -iname '*.cpp')
OBJECTS := $(patsubst ./%, bin/%, $(SOURCES:.cpp=.o))

.PHONY: all
all: $(OBJECTS)
	@echo "Final linking and compilation $(EXECUTABLE)"
	@$(CC) -o $(EXECUTABLE) $^ -lwiringPi $(CFLAGS)

bin/%.o: %.cpp
	@echo "Compiling $@"
	@mkdir -p $(dir $@)
	@$(CC) -c $^ -o $@ $(CFLAGS)

.PHONY: clear
clear:
	@echo "Cleaning Project from compiled files"
	@rm -f $(EXECUTABLE)
	@rm -f -r ./bin

.PHONY: start
start: all
	@echo "Start App"
	@./$(EXECUTABLE)