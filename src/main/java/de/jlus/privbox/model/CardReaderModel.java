package de.jlus.privbox.model;

import javafx.beans.property.*;

/**
 * keeps track of any card-related input and output
 */
public class CardReaderModel {
    private int allowedLengthCharSequence, timeToResetCharSequence;

    private StringBuilder charSequence = new StringBuilder();
    private int timeSinceLastRegistered = 0;
    private int maxTime = 0;
    private double currentTime = 0;
    private final BooleanProperty displayCardRequired = new SimpleBooleanProperty(false);
    private final DoubleProperty progressCardRequired = new SimpleDoubleProperty();
    private final StringProperty userName = new SimpleStringProperty();


    /**
     * constructor stores the values
     * @param allowedLengthCharSequence the allowed number of characters for the serial
     * @param timeToResetCharSequence the time in ms, after which the read values will be removed
     */
    CardReaderModel (int allowedLengthCharSequence, int timeToResetCharSequence) {
        this.allowedLengthCharSequence = allowedLengthCharSequence;
        this.timeToResetCharSequence = timeToResetCharSequence;
    }


    /**
     * registers a char into the charSequence and resets the timeSinceLastRegistered member
     * @param character the char to register
     * @return true, if max length for sequence has reached, false otherwise
     */
    boolean registerCharacter (char character) {
        charSequence.append(character);
        timeSinceLastRegistered = 0;
        return charSequence.length() == allowedLengthCharSequence;
    }


    /**
     * gets the stored value and resets it to its default state (empty)
     * @return the stored value as String
     */
    String getAndResetSerial () {
        final String returner = charSequence.toString();
        charSequence = new StringBuilder();
        return returner;
    }


    /**
     * updates the progress, if any card is required and also the time, since the last character
     * has been added
     * @param deltaT time since the last execution of this method in ms
     */
    void incrementProgress (long deltaT) {
        // update the progress for the required card
        if (displayCardRequired.get()) {
            currentTime += (double) deltaT / 1000;
            if (currentTime >= maxTime)
                displayCardRequired.set(false);
            else {
                progressCardRequired.set(currentTime / maxTime);
            }
        }

        // update the time, since the last character has been read
        // and reset, if full
        if (charSequence.length() > 0) {
            timeSinceLastRegistered += deltaT;
            if (timeSinceLastRegistered > timeToResetCharSequence) {
                charSequence = new StringBuilder();
            }
        }
    }


    /**
     * updates the model with values from the webserver
     * @param newTime 0, if it not to be shown, time in seconds otherwise
     * @param newUserName the new username to show
     */
    void updateModel (int newTime, String newUserName) {
        displayCardRequired.set(newTime != 0);
        maxTime = newTime;
        currentTime = 0;
        progressCardRequired.set(0.0);
        userName.set(newUserName);
    }


    /**
     * gets the Property for whether an active card-reading is in progress or not
     * @return the ReadOnlyBooleanProperty as described above
     */
    public ReadOnlyBooleanProperty getDisplayCardRequiredProperty () {
        return displayCardRequired;
    }


    /**
     * gets the Property for the progress, meaning that is still left
     * @return the ReadOnlyDoubleProperty as described above
     */
    public ReadOnlyDoubleProperty getProgress () {
        return progressCardRequired;
    }


    /**
     * gets the Property for the UserName, which card is being scanned now
     * @return the ReadOnlyStringProperty as described above
     */
    public ReadOnlyStringProperty getUserNameProperty () {
        return userName;
    }
}
