package de.jlus.privbox.controller.dispaycontroller;

import de.jlus.privbox.model.Model;
import javafx.scene.Scene;

/**
 * each Controller takes care of its View and Model
 */
abstract public class Controller {
    Model model;


    /**
     * just saves the model and therefore enforces every Controller to accept a model
     * @param model the model to save
     */
    Controller (Model model) {
        this.model = model;
    }


    /**
     * is being called, when the alarm is supposed to be triggered
     */
    abstract public void handleAlarmEvent ();


    /**
     * gets the newly generated Scene
     * @return the newly generated Scene
     */
    abstract public Scene getScene ();


    /**
     * is being called, when the Stage is closed and closes the task for timing stuff
     */
    abstract public void exit();
}