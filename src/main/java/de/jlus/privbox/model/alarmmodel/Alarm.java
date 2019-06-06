package de.jlus.privbox.model.alarmmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Alarm implements Comparable<Alarm> {
    private boolean isBackground;
    private int alarmId;
    private String description;
    private long timeStampTrigger;
    private IntegerProperty triggerIn;
    private String sound;


    /**
     * constructor creating a new Alarm for adding to the AlarmModel
     * @param isBackground true, if only called in background and not displayed, false otherwise
     * @param alarmId the alarmId from the server
     * @param description the description to display
     * @param triggerIn the seconds to wait until this is being triggered
     * @param sound path to the sound-file to play
     */
    public Alarm (boolean isBackground, int alarmId, String description, int triggerIn, String sound) {
        this.isBackground = isBackground;
        this.alarmId = alarmId;
        this.description = description;
        timeStampTrigger = (System.currentTimeMillis() / 1000) + triggerIn;
        this.triggerIn = new SimpleIntegerProperty(triggerIn);
        this.sound = sound;
    }


    /**
     * getter for the alarmId
     * @return the alarmId from the server
     */
    public int getAlarmId() {
        return alarmId;
    }


    /**
     * getter for the sound-file-path
     * @return path to the sound-file to play, empty String if not to play
     */
    public String getSound() {
        return sound;
    }


    /**
     * getter for the description
     * @return the description to display
     */
    String getDescription() {
        return description;
    }


    /**
     * decrements the value in the triggerIn property, if one second has really passed
     * @return true, if this alarm is to be triggered, false otherwise
     */
    boolean decrementValue () {
        triggerIn.set((int) (timeStampTrigger - (System.currentTimeMillis()/1000)));
        return triggerIn.get() <= 0;
    }


    /**
     * getter for the background
     * @return true, if only called in background and not displayed, false otherwise
     */
    public boolean isBackground () {
        return isBackground;
    }


    /**
     * gets the IntegerProperty
     * @return the time, in which it will trigger
     */
    ReadOnlyIntegerProperty getTriggerIn() {
        return triggerIn;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public int compareTo (Alarm other) {
        return triggerIn.get() - other.triggerIn.get();
    }
}
