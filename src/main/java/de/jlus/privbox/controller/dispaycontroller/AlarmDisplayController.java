package de.jlus.privbox.controller.dispaycontroller;

import de.jlus.privbox.PrivBoxApp;
import de.jlus.privbox.controller.ControllerType;
import de.jlus.privbox.controller.StageManager;
import de.jlus.privbox.model.Model;
import de.jlus.privbox.model.alarmmodel.Alarm;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * displays the AlarmView
 */
public class AlarmDisplayController extends Controller {
    private static final Logger log = Logger.getLogger(AlarmDisplayController.class.getName());
    private Alarm triggeredAlarm;
    private AnimationTimer animationTimer;
    private final DoubleProperty clipProgress = new SimpleDoubleProperty(0.0);


    /**
     * just forward the model to the top Controller
     * @param model the model to save
     */
    public AlarmDisplayController (Model model) {
        super(model);
        triggeredAlarm = model.getAlarmModel().getCurrentlyTriggeredAlarm();

        // only play song, when there is one
        if (triggeredAlarm.getSound().equals(""))
            exitAlarmDisplay();
        else
            playAudio();
    }


    /**
     * update the model, so the new alarm is not in it
     */
    @Override
    public void exit () {
        if (animationTimer != null)
            animationTimer.stop();
        model.reloadData();
    }


    /**
     * {@inheritDoc}
     * Do nothing with the AlarmEvent, when the current alarm is being shown already
     */
    @Override
    public void handleAlarmEvent () {
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Scene getScene () {
        ProgressBar pb = new ProgressBar();
        pb.progressProperty().bind(clipProgress);
        return new Scene(pb);
    }


    /**
     * plays the audio file and updates the progressProperty
     */
    private void playAudio () {
        // play the sound for the current alarm
        try {
            // play the sound
            Clip clip = AudioSystem.getClip();
            File soundFile = new File(triggeredAlarm.getSound());
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(soundFile);
            clip.open(inputStream);
            // make sure the view ends, once this is over
            LineListener ll = event -> {
                if (event.getType() == LineEvent.Type.STOP)
                    exitAlarmDisplay();
            };
            clip.addLineListener(ll);
            clip.start();

            // set the updater
            double totalLength = clip.getFrameLength();
            animationTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    clipProgress.set(clip.getFramePosition() / totalLength);
                }
            };
            animationTimer.start();
        }
        catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            log.log(Level.SEVERE, "Could not play AudioFile for Alarm", e);
        }
    }


    /**
     * exits the AlarmDisplay to the dark display after configured time
     */
    private void exitAlarmDisplay () {
        // stop displaying screen after set time
        Timer goBackToDarkTheme = new Timer();
        goBackToDarkTheme.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() ->
                    StageManager.getInstance().openStage(ControllerType.DARK)
                );
            }
        }, Integer.parseInt(
                PrivBoxApp.getConfigProperties().getProperty("displayAlarmScreenAfterAlarmFor"))
                * 1000L);
    }
}
