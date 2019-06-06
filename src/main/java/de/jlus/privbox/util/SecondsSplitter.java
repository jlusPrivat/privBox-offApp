package de.jlus.privbox.util;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.StringPropertyBase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * enables the developer to bind a Property to format the seconds into a custom design
 */
public class SecondsSplitter extends StringPropertyBase {
    private int days, hours, minutes, seconds;
    private String format;


    /**
     * constructs the SecondsSplitter, which is a StringProperty
     * @param seconds the IntegerProperty representing the seconds
     * @param format the format as described in {@link SecondsSplitter#getFormattedTime()}
     */
    public SecondsSplitter (ReadOnlyIntegerProperty seconds, String format) {
        seconds.addListener(this::listener);
        this.format = format;
    }


    /**
     * the listener splits the seconds into days, hours, minutes and seconds and formats it
     * @param o the Observable that triggered the event
     * @param oldVal the value the Observable stored before it was changed
     * @param newVal the new value, that is about to be stored in the Observable
     */
    private void listener (Observable o, Number oldVal, Number newVal) {
        // split the time
        int secs = newVal.intValue();
        days = secs / 86400;
        secs -= days * 86400;
        hours = secs / 3600;
        secs -= hours * 3600;
        minutes = secs / 60;
        this.seconds = secs - (minutes * 60);

        // set the newly formatted time as the new value for the Property
        set(getFormattedTime());
    }


    /**
     * This method replaces all occurrences of these constants in the format and returns it:
     * - %d: the number of full days
     * - %h: the number of full hours
     * - %m: the number of full minutes
     * - %s: the number of full seconds
     * Optionally days and hours may define a group of characters that will be removed, when
     * the corresponding value is 0:
     * - [%d number of days] and %h hours left
     *
     * @return the String as described above
     */
    private String getFormattedTime () {
        String returner = format;

        // parse the days
        final Matcher daysMatcher = Pattern.compile("\\[%d(.*?)]").matcher(returner);
        if (daysMatcher.find())
            returner = daysMatcher.replaceAll(days == 0 ? "" : days + daysMatcher.group(1));
        else
            returner = returner.replaceAll("%d", Integer.toString(days));

        // parse the hours
        final Matcher hoursMatcher = Pattern.compile("\\[%h(.*?)]").matcher(returner);
        if (hoursMatcher.find())
            returner = hoursMatcher.replaceAll(hours == 0 ? "" : hours + hoursMatcher.group(1));
        else
            returner = returner.replaceAll("%h", Integer.toString(hours));

        // parse the minutes and seconds
        returner = returner.replaceAll("%m", Integer.toString(minutes));
        return returner.replaceAll("%s", Integer.toString(seconds));
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Object getBean () {
        return null;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getName () {
        return "";
    }
}
