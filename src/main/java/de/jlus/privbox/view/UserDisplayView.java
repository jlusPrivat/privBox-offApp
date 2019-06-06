package de.jlus.privbox.view;

import de.jlus.privbox.PrivBoxApp;
import de.jlus.privbox.model.Model;
import de.jlus.privbox.util.SecondsSplitter;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * is responsible for displaying:
 * - The current day, date and time
 * - Currently logged in user
 * - currently running web-login with the remaining time and the user accessing
 * - time to and name of next alarm (if below set value in properties-file)
 */
public class UserDisplayView {
    private final AnchorPane root = new AnchorPane();
    private DateTimeFormatter dateFormatter;
    private final Label textDateTime = new Label();
    private final VBox loggedInBox = new VBox();
    private final Label loggedInLabel = new Label();


    /**
     * generates the View
     * @param model the model to use for the content
     */
    public UserDisplayView (Model model) {
        final Properties lang = PrivBoxApp.getLanguageProperties();
        dateFormatter = DateTimeFormatter.ofPattern(lang.getProperty("dateFormat"));

        // style root and make cursor invisible, if needed
        root.setId("root");
        if (PrivBoxApp.getConfigProperties().getProperty("deployed").equals("true"))
        root.setCursor(new ImageCursor(
                new Image(getClass().getResource("../Transparent.gif").toExternalForm())));

        // add the date and time text
        textDateTime.setId("date_time_label");
        root.getChildren().add(textDateTime);
        AnchorPane.setTopAnchor(textDateTime, 10d);
        AnchorPane.setRightAnchor(textDateTime, 10d);

        // add the currently logged in user
        final Label loggedInHeading = new Label(lang.getProperty("loggedInUser"));
        loggedInHeading.setId("logged_in_heading");
        ReadOnlyStringProperty loggedInProperty = model.getCurrentlyLoggedInProperty();
        updateCurrentlyLoggedIn(loggedInProperty.get());
        loggedInProperty.addListener((observable, oldValue, newValue) ->
            updateCurrentlyLoggedIn(newValue)
        );
        loggedInLabel.setId("logged_in_label");
        loggedInBox.getChildren().addAll(loggedInHeading, loggedInLabel);
        root.getChildren().add(loggedInBox);
        AnchorPane.setTopAnchor(loggedInBox, 10d);
        AnchorPane.setLeftAnchor(loggedInBox, 10d);

        // add the ProgressIndicator for the active web-logins
        final ProgressIndicator activeWebLoginIndicator = new ProgressIndicator();
        activeWebLoginIndicator.progressProperty().bind(model.getCardReaderModel().getProgress());
        final Circle progressInnerCircle = new Circle(125);
        progressInnerCircle.setId("progress_inner_circle");
        final StackPane progressStackPane = new StackPane(activeWebLoginIndicator, progressInnerCircle);
        final Label activeWebLoginLabel = new Label();
        activeWebLoginLabel.setId("active_web_login_label");
        activeWebLoginLabel.textProperty().bind(model.getCardReaderModel().getUserNameProperty());
        final VBox activeWebLoginBox = new VBox(progressStackPane, activeWebLoginLabel);
        activeWebLoginBox.setAlignment(Pos.CENTER);
        activeWebLoginBox.setVisible(false);
        model.getCardReaderModel().getDisplayCardRequiredProperty()
                .addListener((observable, oldValue, newValue) -> activeWebLoginBox.setVisible(newValue));
        root.getChildren().add(activeWebLoginBox);
        AnchorPane.setBottomAnchor(activeWebLoginBox, 20d);
        AnchorPane.setRightAnchor(activeWebLoginBox, 20d);

        // add the time to and name of the next alarm
        final Label nextAlarmHeading = new Label(lang.getProperty("nextAlarmIn"));
        nextAlarmHeading.setId("next_alarm_heading");
        final Label nextAlarmName = new Label();
        nextAlarmName.setId("next_alarm_name");
        nextAlarmName.textProperty().bind(model.getAlarmModel().getNextAlarmDescriptionProperty());
        final Label nextAlarmTriggerIn = new Label();
        nextAlarmTriggerIn.setId("next_alarm_trigger_in");
        final SecondsSplitter formattedTimeProperty =
                new SecondsSplitter(model.getAlarmModel().getNextAlarmTriggerInProperty(),
                lang.getProperty("nextAlarmFormat"));
        nextAlarmTriggerIn.textProperty().bind(formattedTimeProperty);
        final VBox nextAlarmBox = new VBox(nextAlarmHeading, nextAlarmName, nextAlarmTriggerIn);
        if (!model.getAlarmModel().getNextAlarmIsSetProperty().get())
            nextAlarmBox.setVisible(false);
        model.getAlarmModel().getNextAlarmIsSetProperty().addListener(((observable, oldValue, newValue) ->
            nextAlarmBox.setVisible(newValue)
        ));
        root.getChildren().add(nextAlarmBox);
        AnchorPane.setBottomAnchor(nextAlarmBox, 20d);
        AnchorPane.setLeftAnchor(nextAlarmBox, 20d);
    }


    /**
     * this method must be called every second, so it can update the textDateTime
     */
    public void updateView () {
        textDateTime.setText(dateFormatter.format(LocalDateTime.now()));
    }


    /**
     * gets the root for creating the scene
     * @return the Parent containing all fields
     */
    public Parent getRoot () {
        return root;
    }


    /**
     * updates the Text display for the username. May set it invisible.
     * @param username null, if no user is logged in, the String otherwise
     */
    private void updateCurrentlyLoggedIn (String username) {
        if (username == null)
            loggedInBox.setVisible(false);
        else {
            loggedInLabel.setText(username);
            loggedInBox.setVisible(true);
        }
    }
}
