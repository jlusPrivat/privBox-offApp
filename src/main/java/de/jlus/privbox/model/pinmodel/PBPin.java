package de.jlus.privbox.model.pinmodel;

/**
 * just a holder for the a pins type and state
 */
class PBPin {
    private PinType type;
    private WriteAction writeAction;
    private boolean state;


    /**
     * constructs the PBPin
     * @param type the PinType of this pin
     * @param initalState the first State it has
     */
    PBPin(PinType type, boolean initalState, WriteAction writeAction) {
        this.type = type;
        this.writeAction = writeAction;
        state = initalState;
    }


    /**
     * gets the type of this pin
     * @return the PinType
     */
    PinType getType () {
        return type;
    }


    /**
     * gets the state of the pin
     * @return the boolean representing the current state
     */
    boolean getState () {
        return state;
    }


    /**
     * calls the predefined action of the pin for writing and updates the current state
     *
     * @param newState the new state of the pins
     */
    void write (boolean newState) {
        state = newState;
        if (writeAction != null)
            writeAction.trigger(newState);
    }
}
