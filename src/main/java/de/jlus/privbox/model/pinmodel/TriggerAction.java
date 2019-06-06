package de.jlus.privbox.model.pinmodel;

public interface TriggerAction {
    /**
     * the action to be triggered, when a pin changes
     * @param gpio the gpio port as of the privbox-numeration
     * @param PorR either the character P for push action or R for release action
     */
    void trigger (int gpio, char PorR);
}
