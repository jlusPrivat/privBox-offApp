package de.jlus.privbox.model.webcommunicator;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Webcommunicator manages all communication between the Webserver and this application.
 * Any Communication to the communicator have this form:
 * - ASCII-Only
 * - Key1=Val1:;:Key2=Val2 ...
 */
public class Webcommunicator {
    private static Logger log = Logger.getLogger(Webcommunicator.class.getName());
    private MessageProcessor messageProcessor;
    private String baseURL; // like "http://localhost/privbox/controller.php"


    /**
     * Constructs the Webcommunicator by creating a listener on specified port
     * @param configProperties Properties to use for the port specification
     * @param mp MessageProcessor to use when a new Message was received
     */
    public Webcommunicator (Properties configProperties, MessageProcessor mp) {
        // save the members
        messageProcessor = mp;
        baseURL = configProperties.getProperty("serverURL");

        // create the thread for listening to the port
        Thread daemon = new Thread(() -> {
            int port = Integer.parseInt(configProperties.getProperty("socketPort"));
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                do {
                    // accept only one connection at a time
                    Socket receiver = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(receiver.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String inputLine;
                    while ((inputLine = reader.readLine()) != null)
                        stringBuilder.append(inputLine);
                    receiver.close();

                    // Process the value
                    Map<String, String> argument = processInput(stringBuilder.toString());
                    Platform.runLater(() ->
                        messageProcessor.processMessage(MessageType.SOCKETINPUT, argument)
                    );
                } while (true);
            }
            catch (IOException e) {
                log.log(Level.SEVERE, "IOException while creating daemon", e);
            }
        });
        daemon.setDaemon(true);
        daemon.start();
    }


    /**
     * This method sends a Map of Strings to the server. Any returns will be handled
     * by the MessageProcessor.
     * @param args the arguments to pass to this server
     */
    public void sendToServer (Map<String, String> args) {
        // generate the GET-Header for the request
        StringBuilder stringBuilder = new StringBuilder(baseURL).append("?");
        Iterator<Map.Entry<String, String>> iterator = args.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            if (iterator.hasNext())
                stringBuilder.append("&");
        }

        try {
            // connect to the server
            URL url = new URL(stringBuilder.toString());
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            // read the lines and close connection
            stringBuilder.delete(0, stringBuilder.length());
            String line;
            while ((line = reader.readLine()) != null)
                stringBuilder.append(line);
            reader.close();

            // let the messageProcessor handle the response
            messageProcessor.processMessage(MessageType.HTTPRESPONSE,
                    processInput(stringBuilder.toString()));
        }
        catch (IOException e) {
            log.log(Level.SEVERE, "Could not open connection to server", e);
        }
    }


    /**
     * generates a Map with separated keys and values
     * @param input the String from the Server
     * @return the Map containing the parsed values
     */
    private static Map<String, String> processInput (String input) {
        Map<String, String> returner = new HashMap<>();
        try {
            String[] entries = input.split(":;:");
            for (String entry : entries) {
                String[] splitted = entry.split("=", -1);
                if (splitted.length != 2)
                    throw new IndexOutOfBoundsException();
                returner.put(splitted[0], splitted[1]);
            }
        }
        catch (IndexOutOfBoundsException e) {
            log.warning("Unexpected input from " + input);
        }
        return returner;
    }
}
