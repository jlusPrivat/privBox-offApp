package de.jlus.privbox.model.pinmodel;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.system.SystemInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * controls all pinActivities
 */
public class PinModel {
    private static final Logger log = Logger.getLogger(PinModel.class.getName());

    private final GpioController gpioController = GpioFactory.getInstance();
    private final Map<Integer, PBPin> gpios = new HashMap<>();


    /**
     * Sets the mode for any pin.
     * Designed for Raspberry Pi Model B Revision 2.0
     * Pins 15 and 16 may not be assigned (those are the UART-Pins)
     * Pins 0 to 16 are pins on the Rasp itself
     * Pins 17 to infinite are pins on the UART-connected device
     *
     * @param gpio the gpio port
     * @param type the type of the port
     * @param triggerAction The TriggerAction to be called, when the pin changes its state
     */
    public void setPinMode (int gpio, PinType type, TriggerAction triggerAction) {
        log.info("PinMode " + gpio + " set to " + type.toString());

        if (gpio < 0 || gpio == 15 || gpio == 16) {
            log.warning("PBPin number is illegal and may not be set: " + gpio);
            return;
        }

        final TriggerAction action = triggerAction == null ? (a, b) -> {} : triggerAction;
        WriteAction writeAction = null;
        if (gpio <= 20) {
            // set the pin for direct transaction
            final Pin pin = RaspiPin.allPins(SystemInfo.BoardType.RaspberryPi_B_Rev2)[gpio];
            switch (type) {
                case INPUT_BUTTON:
                    final GpioPinDigitalInput inputButton =
                            gpioController.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);
                    inputButton.setDebounce(50);
                    inputButton.addListener((GpioPinListenerDigital) event ->
                            action.trigger(gpio, event.getEdge() == PinEdge.FALLING ? 'P' : 'R'));
                    break;
                case INPUT_SWITCH:
                    final GpioPinDigitalInput inputSwitch =
                            gpioController.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);
                    inputSwitch.setDebounce(50);
                    inputSwitch.addListener((GpioPinListenerDigital) event ->
                            action.trigger(gpio, event.getEdge() == PinEdge.FALLING ? 'P' : 'R'));
                    break;
                case INPUT_VIRTUAL_LATCHED:
                    final GpioPinDigitalInput inputVirtualLatch =
                            gpioController.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);
                    inputVirtualLatch.setDebounce(50);
                    inputVirtualLatch.addListener((GpioPinListenerDigital) event -> {
                        if (event.getEdge() == PinEdge.FALLING) {
                            final PBPin pbPin = gpios.get(gpio);
                            pbPin.write(!pbPin.getState());
                            action.trigger(gpio, pbPin.getState() ? 'P' : 'R');
                        }
                    });
                    break;
                case OUTPUT:
                    final GpioPinDigitalOutput output =
                            gpioController.provisionDigitalOutputPin(pin, PinState.LOW);
                    writeAction = newState -> {
                        action.trigger(gpio, newState ? 'P' : 'R');
                        if (newState) output.high();
                        else output.low();
                    };
                    break;
            }
        }
        else {
            // set the pin on the UART-connected device
            // !!!
            switch (type) {
                case INPUT_BUTTON:
            }
        }

        // add the new gpio to the list
        gpios.put(gpio, new PBPin(type, false, writeAction));
    }


    /**
     * This method sets the pin either high or low.
     * If the pin is configured as inputVirtualLatched, this will update the state
     * Any other PinTypes will be ignored
     *
     * @param gpio the gpio port
     * @param value true for high, false for low
     */
    public void writePin (int gpio, boolean value) {
        log.info("PinValue " + gpio + " set to " + (value ? "on" : "off"));

        if (gpios.containsKey(gpio))
            gpios.get(gpio).write(value);
        else
            log.warning("Tried to write to pin " + gpio
                    + " even when it was not initialized");
    }


    /**
     * clears the current configurations, so another set of configurations can be loaded
     */
    public void clearConfiguration () {
        for (GpioPin pin: gpioController.getProvisionedPins())
            gpioController.unprovisionPin(pin);
    }
}
