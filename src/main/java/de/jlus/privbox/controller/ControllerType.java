package de.jlus.privbox.controller;

import de.jlus.privbox.controller.dispaycontroller.AlarmDisplayController;
import de.jlus.privbox.controller.dispaycontroller.Controller;
import de.jlus.privbox.controller.dispaycontroller.DarkUserDisplayController;
import de.jlus.privbox.controller.dispaycontroller.LightUserDisplayController;
import de.jlus.privbox.model.Model;

public enum ControllerType {
    DARK(DarkUserDisplayController::new),
    LIGHT(LightUserDisplayController::new),
    ALARM(AlarmDisplayController::new);


    /**
     * applying the Factory pattern to an enumeration
     * @param factory the constructor for the Controller
     */
    ControllerType (ControllerFactory factory) {
        this.factory = factory;
    }

    ControllerFactory factory;
    interface ControllerFactory {
        Controller constructController (Model model);
    }
}
