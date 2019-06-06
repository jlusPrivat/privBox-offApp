package de.jlus.privbox.controller.dispaycontroller;

import de.jlus.privbox.PrivBoxApp;
import de.jlus.privbox.controller.ControllerType;
import de.jlus.privbox.controller.StageManager;
import de.jlus.privbox.model.Model;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class DarkUserDisplayController extends UserDisplayController {
    private final Timer controllerTimer = new Timer(true);


    /**
     * just forward the model to the top Controller
     * @param model the model to save
     */
    public DarkUserDisplayController (Model model) {
        super(model);

        // the timer for switching designs
        final Properties config = PrivBoxApp.getConfigProperties();
        final int displayLightThemeStart = Integer.parseInt(config.getProperty("displayLightThemeStart"));
        final long now = LocalDateTime.now().toLocalTime().toSecondOfDay();
        long delay = displayLightThemeStart - now;
        if (now > displayLightThemeStart)
            // schedule for tomorrow
            delay += 60*60*24;
        controllerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> StageManager.getInstance().openStage(ControllerType.LIGHT));
            }
        }, delay * 1000);
    }


    /**
     * {@inheritDoc}
     */
    void applyCss () {
        scene.getStylesheets().add(this.getClass()
                .getResource("/de/jlus/privbox/darkUserDisplayView.css").toExternalForm());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void exit () {
        super.exit();
        controllerTimer.cancel();
    }
}
