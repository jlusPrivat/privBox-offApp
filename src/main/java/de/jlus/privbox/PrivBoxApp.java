package de.jlus.privbox;

import de.jlus.privbox.controller.StageManager;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.*;
import java.util.logging.*;
import java.util.Properties;

/**
 * Main class to load on startup
 */
public class PrivBoxApp extends Application {
    private static Logger log = Logger.getLogger(PrivBoxApp.class.getName());
    private static Properties configProperties;
    private static Properties languageProperties;


    /**
     * only starts javafx
     * @param args arguments on starting the application
     */
    public static void main (String[] args) {
        launch(args);
    }


    /**
     * starts the gui
     * @param mainStage the current main stage, to be filled and shown
     */
    @Override
    public void start (Stage mainStage) {
        configureLogger();
        StageManager.createInstance(mainStage);
    }


    /**
     * configures the Logger so, that lower messages will be displayed in a nice way
     */
    private void configureLogger () {
        Logger rootLog = Logger.getLogger("");
        rootLog.setLevel(Level.INFO);

        // make the rootConsoleHandler display and write only the important ones
        Handler rootHandler = rootLog.getHandlers()[0];
        rootHandler.setLevel(Level.WARNING);

        // create a new logger for the lower ones
        ConsoleHandler infoHandler = new ConsoleHandler();
        infoHandler.setLevel(Level.INFO);
        infoHandler.setFilter((record) ->
                record.getLevel().intValue() < Level.WARNING.intValue()
        );
        Formatter formatterForInfoHandler = new Formatter() {
            @Override
            public String format(LogRecord record) {
                // the fine stuff in green
                return  record.getLoggerName() + " - "
                        + record.getLevel().toString() + ": " + record.getMessage()  + "\n";
            }
        };
        infoHandler.setFormatter(formatterForInfoHandler);
        rootLog.addHandler(infoHandler);
    }


    /**
     * gets the loaded Properties object
     * @return the Properties object
     */
    public static Properties getConfigProperties () {
        if (configProperties == null) {
            configProperties = new Properties();
            try {
                configProperties.load(PrivBoxApp.class.getResourceAsStream("config.properties"));
            }
            catch (IOException e) {
                log.log(Level.SEVERE, "Could not load config file", e);
            }
        }
        return configProperties;
    }


    /**
     * loads the language file for the UserDisplayView
     * @return the Properties containing the language props
     */
    public static Properties getLanguageProperties () {
        if (languageProperties == null) {
            languageProperties = new Properties();
            final Properties config = PrivBoxApp.getConfigProperties();
            String path = config.getProperty("languagePath")
                    + config.getProperty("language") + "-lang.properties";
            try {
                languageProperties.load(PrivBoxApp.class.getResourceAsStream(path));
            } catch (IOException e) {
                log.log(Level.SEVERE, "IO Exception while reading language file", e);
            }
        }
        return languageProperties;
    }
}
