package de.jlus.privbox.model.alarmmodel;

import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;


/**
 * represents the Alarm in the Model
 * Observable gets triggered, when Alarm is triggered.
 * The Observer gets the Alarm that set off the trigger as argument
 */
public class AlarmModel extends Observable {
    private final List<Alarm> alarmCollection = new ArrayList<>();

    // All these properties refer to the next showable alarms. Background alarms are not touched by this.
    private Alarm currentlyTriggeredAlarm;
    private final BooleanProperty nextAlarmIsSet = new SimpleBooleanProperty();
    private final StringProperty nextAlarmDescription = new SimpleStringProperty();
    private final IntegerProperty nextAlarmTriggerIn = new SimpleIntegerProperty();


    /**
     * changes the value of trigger now and informs any Observers
     */
    public void triggerNow () {
        // pass the topmost alarm
        currentlyTriggeredAlarm = alarmCollection.get(0);
        setChanged();
        notifyObservers();
    }


    /**
     * This method has to be called once every second.
     * It decrements the timer of the alarm and returns, if it has to be triggered now
     * @return Alarm if this has to be triggered now, null
     */
    public Alarm decrementAlarmTimer () {
        for (Alarm entry : alarmCollection) {
            // if it may be triggered now
            if (entry.decrementValue())
                return entry;
        }
        return null;
    }


    /**
     * removes all alarms from the collection
     */
    public void clearAllAlarms () {
        nextAlarmIsSet.set(false);
        alarmCollection.clear();
    }


    /**
     * adds a new alarm to the collection and sorts it
     * @param alarm the new Alarm to add
     */
    public void addNewAlarm (Alarm alarm) {
        alarmCollection.add(alarm);
        Alarm nextAlarm = getNextShowableAlarm();
        nextAlarmIsSet.set(nextAlarm != null);
        if (nextAlarm != null) {
            nextAlarmDescription.set(nextAlarm.getDescription());
            nextAlarmTriggerIn.unbind();
            nextAlarmTriggerIn.bind(nextAlarm.getTriggerIn());
        }
    }


    /**
     * gets the Alarm, that was most recently triggered
     * @return the Alarm that was triggered
     */
    public Alarm getCurrentlyTriggeredAlarm() {
        return currentlyTriggeredAlarm;
    }


    /**
     * gets the Property for whether the next showable Alarm is present or not
     * @return the specified ReadOnlyBooleanProperty
     */
    public ReadOnlyBooleanProperty getNextAlarmIsSetProperty () {
        return nextAlarmIsSet;
    }


    /**
     * gets the Property for the description of the next showable alarm
     * @return the specified ReadOnlyStringProperty
     */
    public ReadOnlyStringProperty getNextAlarmDescriptionProperty () {
        return nextAlarmDescription;
    }


    /**
     * gets the Property for the time in seconds, in which the next showable alarm will trigger
     * @return the specified ReadOnlyIntegerProperty
     */
    public ReadOnlyIntegerProperty getNextAlarmTriggerInProperty () {
        return nextAlarmTriggerIn;
    }


    /**
     * gets the next Alarm, which will be shown
     * @return null, if no such alarm is present, the Alarm otherwise
     */
    private Alarm getNextShowableAlarm () {
        Collections.sort(alarmCollection);
        for (Alarm entry : alarmCollection) {
            if (!entry.isBackground())
                return entry;
        }
        return null;
    }
}
