package de.jlus.privbox.controller.dispaycontroller;

import de.jlus.privbox.controller.ControllerType;
import de.jlus.privbox.controller.StageManager;
import de.jlus.privbox.model.Model;
import de.jlus.privbox.view.UserDisplayView;
import javafx.application.Platform;
import javafx.scene.Scene;
import java.util.*;

/**
 * The Controller for both, the dark and the light scene.
 */
abstract class UserDisplayController extends Controller {
    Scene scene;
    private final Timer viewTimer = new Timer(true);


    /**
     * just forward the model to the top Controller
     * @param model the model to save
     */
    UserDisplayController (Model model) {
        super(model);
        final UserDisplayView view = new UserDisplayView(model);
        scene = new Scene(view.getRoot());
        applyCss();

        // the timer for the time to update
        viewTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(view::updateView);
            }
        }, 0L, 1000L);
    }


    /**
     * apply the correct css to the root element of the view
     */
    abstract void applyCss ();


    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAlarmEvent () {
        // run later to be in the JavaFx Thread
        if (!model.getAlarmModel().getCurrentlyTriggeredAlarm().isBackground()) {
            Platform.runLater(() ->
                    StageManager.getInstance().openStage(ControllerType.ALARM)
            );
        }
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Scene getScene () {
        return scene;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void exit () {
        viewTimer.cancel();
    }
}
