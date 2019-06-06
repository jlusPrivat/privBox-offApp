package de.jlus.privbox.controller;

import de.jlus.privbox.PrivBoxApp;
import de.jlus.privbox.controller.dispaycontroller.Controller;
import de.jlus.privbox.model.Model;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Properties;

/**
 * manager of the Mainstage
 */
public class StageManager {
    private static StageManager instance;
    private Model model;
    private Controller controller;
    private Stage stage;


    /**
     * creates a new StageManager, opens the default Scene and returns the StageManager
     */
    public static void createInstance (Stage mainStage) {
        if (instance != null)
            throw new IllegalStateException("The instance can only be created once");
        instance = new StageManager(mainStage);
    }


    /**
     * gets the Singleton
     * @return the StageManager instance
     */
    public static StageManager getInstance () {
        if (instance == null)
            throw new IllegalStateException("The instance has to be created first");
        return instance;
    }


    /**
     * just make the constructor for the Singleton private and call the main
     */
    private StageManager (Stage mainStage) {
        final Properties props = PrivBoxApp.getConfigProperties();
        stage = mainStage;
        model = new Model(props);
        openStage(ControllerType.DARK);


        // decrement the timer every second
        final AnimationTimer animationTimer = new AnimationTimer() {
            private long nanoSec = 0;
            @Override
            public void handle(long now) {
                model.updateTimer((now-nanoSec) / 1000000);
                nanoSec = now;
            }
        };
        animationTimer.start();

        // listen, if alarm is to be triggered now
        model.getAlarmModel().addObserver(((o, argument) ->
                controller.handleAlarmEvent()
        ));

        stage.setTitle("PrivBox");
        stage.setResizable(false);
        stage.setWidth(Integer.parseInt(props.getProperty("screenWidth")));
        stage.setHeight(Integer.parseInt(props.getProperty("screenHeight")));
        stage.setFullScreenExitHint("");
        stage.setFullScreen(props.getProperty("deployed").equals("true"));
        stage.show();
    }


    /**
     * opens another stage
     * @param type the type of stage to open
     */
    public void openStage (ControllerType type) {
        // creates the new Controller and exits the old one
        if (controller != null)
            controller.exit();
        controller = type.factory.constructController(model);
        final Scene rootScene = controller.getScene();

        // add the handler for the serial to read via HID
        rootScene.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if (Character.isDigit(c))
                model.registerSerialChar(c);
        });

        // sets the new scene for the stage
        stage.setScene(rootScene);
    }
}
