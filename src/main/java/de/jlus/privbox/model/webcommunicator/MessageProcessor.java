package de.jlus.privbox.model.webcommunicator;

import java.util.Map;

/**
 * Interface to implement when a Message is received
 */
public interface MessageProcessor {
    /**
     * method being called when the communicator receives a message
     * @param type the MessageType to process
     * @param message Array of Strings containing all parts of this message
     */
    void processMessage (MessageType type, Map<String, String> message);
}
