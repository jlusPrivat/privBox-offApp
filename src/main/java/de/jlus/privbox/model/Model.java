package de.jlus.privbox.model;

import de.jlus.privbox.model.alarmmodel.Alarm;
import de.jlus.privbox.model.alarmmodel.AlarmModel;
import de.jlus.privbox.model.pinmodel.PinModel;
import de.jlus.privbox.model.pinmodel.PinType;
import de.jlus.privbox.model.pinmodel.TriggerAction;
import de.jlus.privbox.model.webcommunicator.Webcommunicator;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.*;
import java.util.logging.Logger;

/**
 * The model manages and stores all types of communication and stuff
 */
public class Model {
    private static final Logger log = Logger.getLogger(Model.class.getName());
    private long lastTriggeredSecond = 0;

    private Webcommunicator communicator;
    private final PinModel pinModel = new PinModel();
    private final AlarmModel alarmModel = new AlarmModel();
    private CardReaderModel cardReaderModel;
    private final StringProperty currentlyLoggedIn = new SimpleStringProperty(null);


    /**
     * constructs the Model, sends the first message and sets the communicator up
     */
    public Model (Properties systemProperties) {
        // create the CardReaderModel
        final int serialLength = Integer.parseUnsignedInt(systemProperties.getProperty("serialLength"));
        final int maxTimeToRead = Integer.parseUnsignedInt(systemProperties.getProperty("serialMaxTimeToRead"));
        cardReaderModel = new CardReaderModel(serialLength, maxTimeToRead);

        // create the communicator
        communicator = new Webcommunicator(systemProperties, ((type, message) -> {
            switch (type) {
                case SOCKETINPUT:
                    if (!message.containsKey("action")) {
                        log.warning("No key \"action\" found in SOCKETINPUT message");
                        break;
                    }
                    switch (message.get("action")) {
                        case "UPDATE_EVERYTHING": reloadData(); break;
                        case "ALARMS_UPDATED": updateAlarm(message); break;
                        case "SWITCH_PINS": switchPins(message); break;
                        case "CARD_REQUIRED": updateCardReaderModel(message); break;
                        case "LOGIN_LOGOUT_DESK": updateCurrentlyLoggedInUser(message); break;
                        default: log.warning("Unknown action received from socket");
                    }
                    break;
                case HTTPRESPONSE:
                    evalualteAllDataFromServer(message);
                    break;
                default:
                    log.warning("Received unknown type of communication");
            }
        }));

        // send the active-message
        Map<String, String> initialArgs = new HashMap<>();
        initialArgs.put("action", "START_UP");
        communicator.sendToServer(initialArgs);
    }


    /**
     * This method has to be called once every second.
     * It decrements the timer of the alarm and triggers it, if needed.
     * It also increments the progress of the cardReaderModel
     *
     * @param deltaT time since the last execution of this method in ms
     */
    public void updateTimer (long deltaT) {
        if ((lastTriggeredSecond += deltaT) >= 1000) {
            lastTriggeredSecond = 0;
            // update the alarm
            Alarm nextAlarm;
            if ((nextAlarm = alarmModel.decrementAlarmTimer()) != null) {
                Map<String, String> serverArgs = new HashMap<>();
                serverArgs.put("action", "TRIGGER_ALARM");
                serverArgs.put("id", Integer.toString(nextAlarm.getAlarmId()));
                communicator.sendToServer(serverArgs);
            }
        }
        // update the progress of the cardReaderModel
        cardReaderModel.incrementProgress(deltaT);
    }


    /**
     * registers a single char to the CardReaderModel and triggers the card scanned event on the webserver,
     * if the requirements for a valid serial have been met
     * @param serialChar the char to register
     */
    public void registerSerialChar (char serialChar) {
        if (cardReaderModel.registerCharacter(serialChar)) {
            final Map<String, String> args = new HashMap<>();
            args.put("action", "CARD_SCANNED");
            args.put("serial", cardReaderModel.getAndResetSerial());
            log.info("Serial read: " + args.get("serial"));
            communicator.sendToServer(args);
        }
    }


    /**
     * sends a request to the server to reload everything
     */
    public void reloadData () {
        Map<String, String> initialArgs = new HashMap<>();
        initialArgs.put("action", "RELOAD_DATA");
        communicator.sendToServer(initialArgs);
    }


    /**
     * getter for the AlarmModel
     * @return the AlarmModel of this Model
     */
    public AlarmModel getAlarmModel () {
        return alarmModel;
    }


    /**
     * getter for the cardReaderModel
     * @return the CardReaderModel of this Model
     */
    public CardReaderModel getCardReaderModel () {
        return cardReaderModel;
    }


    /**
     * Getter for the username, which is currently logged in to the desk.
     * The content is null, if no user is currently logged in.
     * @return the ReadOnlyStringProperty like described above
     */
    public ReadOnlyStringProperty getCurrentlyLoggedInProperty () {
        return currentlyLoggedIn;
    }


    /**
     * This method sets the alarm, the pinmodes and the pinvalues.
     * Requires a transmission with these values:
     * - outPins=1,2,3,4
     * - buttonPins=5,4
     * - switchPins=6,8
     * - virtualLatchPins=6,5
     * - onPins=1,2,3,4,5,6
     * - offPins=1,3,2,4,5
     * - alarmsIds=0,1,2
     * - alarmsDescriptions=testalarm1,testalarm2,testalarm3
     * - alarmsAreBackground=yes,no,yes
     * - alarmsTriggerIn=10035,110,230 (in seconds)
     * - alarmsSounds=StringAbsolutePathToWav,,
     * If no next alarm is set, the Id has to be empty.
     * In the beginning every used pin should have a state set (on or off)
     *
     * @param args the passed arguments by the server
     */
    private void evalualteAllDataFromServer (Map<String, String> args) {
        if (!args.containsKey("outPins") || !args.containsKey("buttonPins") ||
                !args.containsKey("switchPins") || !args.containsKey("virtualLatchPins")) {
            log.warning("Not all required arguments passed");
            return;
        }

        // reset the pinModelcation
        pinModel.clearConfiguration();

        final TriggerAction action = (gpio, PorR) -> {
            final HashMap<String, String> out = new HashMap<>();
            out.put("action", "TRIGGER_PIN");
            out.put("gpio", Integer.toString(gpio));
            out.put("PorR", Character.toString(PorR));
            communicator.sendToServer(out);
        };

        // set all output pins
        String[] outputPins = args.get("outPins").split(",");
        for (String outputPin : outputPins)
            if (!outputPin.equals(""))
                pinModel.setPinMode(Integer.parseInt(outputPin), PinType.OUTPUT, null);

        // set all button-pins
        String[] buttonPins = args.get("buttonPins").split(",");
        for (String buttonPin : buttonPins)
            if (!buttonPin.equals(""))
                pinModel.setPinMode(Integer.parseInt(buttonPin), PinType.INPUT_BUTTON, action);

        // set all switch pins
        String[] switchPins = args.get("switchPins").split(",");
        for (String switchPin : switchPins)
            if (!switchPin.equals(""))
                pinModel.setPinMode(Integer.parseInt(switchPin), PinType.INPUT_SWITCH, action);

        // set all virtual latch pins
        String[] virtualLatchPins = args.get("virtualLatchPins").split(",");
        for (String virtualLatchPin : virtualLatchPins)
            if (!virtualLatchPin.equals(""))
                pinModel.setPinMode(Integer.parseInt(virtualLatchPin), PinType.INPUT_VIRTUAL_LATCHED, action);

        // update the alarm and the pinStates
        switchPins(args);
        updateAlarm(args);
    }


    /**
     * This method sets the alarm with the new value.
     * Requires a transmission with these values:
     * - alarmsIds=0,1,2
     * - alarmsDescriptions=testalarm1,testalarm2,testalarm3
     * - alarmsAreBackground=yes,no,yes
     * - alarmsTriggerIn=10035,110,230 (in seconds)
     * - alarmsSounds=StringAbsolutePathToWav,,
     *
     * @param args the passed arguments by the server
     */
    private void updateAlarm (Map<String, String> args) {
        if (!args.containsKey("alarmsIds") || !args.containsKey("alarmsDescriptions")
                || !args.containsKey("alarmsAreBackground") || !args.containsKey("alarmsTriggerIn")
                || !args.containsKey("alarmsSounds")) {
            log.warning("Not all required arguments passed");
            return;
        }

        // trigger the alarm now
        if (args.getOrDefault("triggerAlarm", "no").equals("yes"))
            alarmModel.triggerNow();

        // update the new alarms
        String[] alarmsIds = args.get("alarmsIds").split(",");
        String[] alarmsDescriptions = args.get("alarmsDescriptions").split(",");
        String[] alarmsAreBackground = args.get("alarmsAreBackground").split(",");
        String[] alarmsTriggerIn = args.get("alarmsTriggerIn").split(",");
        String[] alarmsSounds = args.get("alarmsSounds").split(",");
        if (alarmsIds.length != alarmsDescriptions.length
                || alarmsIds.length != alarmsAreBackground.length
                || alarmsIds.length != alarmsTriggerIn.length
                || alarmsIds.length != alarmsSounds.length) {
            log.warning("Unequal size of parameter passed by server");
            return;
        }

        alarmModel.clearAllAlarms();
        for (int i = 0; i < alarmsIds.length; i++) {
            String alarmId = alarmsIds[i];
            if (alarmId.equals(""))
                continue;
            boolean isBackground = alarmsAreBackground[i].equals("yes");
            alarmModel.addNewAlarm(new Alarm(isBackground, Integer.parseInt(alarmId),
                    alarmsDescriptions[i], Integer.parseInt(alarmsTriggerIn[i]), alarmsSounds[i]));
        }
    }


    /**
     * This method can switch multiple (inputs or outputs) either on or off.
     * Requires a transmission like this example:
     * - onPins=1,2,3,4,5,6
     * - offPins=1,3,2,4,5
     *
     * @param args the passed arguments by the server
     */
    private void switchPins (Map<String, String> args) {
        if (!args.containsKey("onPins") || !args.containsKey("offPins")) {
            log.warning("Not all required arguments passed");
            return;
        }

        // evaluate the onPins
        for (String pin : args.get("onPins").split(","))
            if (!pin.equals(""))
                pinModel.writePin(Integer.parseInt(pin), true);

        // evaluate the offPins
        for (String pin : args.get("offPins").split(","))
            if (!pin.equals(""))
                pinModel.writePin(Integer.parseInt(pin), false);
    }


    /**
     * This method will be called, if a card is expected by the server.
     * Requires a transmission like this example:
     * - newTime=(0, if do not show; anything positive else otherwise)
     * - newUserName=anotherUser
     *
     * @param args the passed arguments by the server
     */
    private void updateCardReaderModel (Map<String, String> args) {
        if (!args.containsKey("newTime") || !args.containsKey("newUserName")) {
            log.warning("Not all required arguments passed");
            return;
        }

        cardReaderModel.updateModel(Integer.parseInt(args.get("newTime")), args.get("newUserName"));
    }


    /**
     * This method will be called, if a user logged in or out from the desk.
     * Requires a transmission like this example:
     * - username=testuser (empty, when current user logged out)
     *
     * @param args the passed arguments by the server
     */
    private void updateCurrentlyLoggedInUser (Map<String, String> args) {
        if (!args.containsKey("username")) {
            log.warning("Not all required arguments passed");
            return;
        }

        final String username = args.get("username");
        currentlyLoggedIn.set(username.equals("") ? null : username);
    }
}
