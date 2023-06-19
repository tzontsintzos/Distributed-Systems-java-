package src;

import src.gpx.Session;
import src.gpx.SessionMetadata;
import src.utils.Utilities;
import src.utils.requests.Request;
import src.utils.requests.UserDataRequest;
import src.utils.requests.XMLProcessingRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//simulates a client that can send requests to the system and receive responses.

public class MockClient {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        switch (Integer.parseInt(args[0])) {
            case 0 -> sendData(args[1]);   //XMLProcessingRequest
            case 1 -> requestData(args[1]);   //UserDataRequest
            default -> throw new RuntimeException("Unknown command int");
        }
    }

    //takes a file name as an argument, creates an XMLProcessingRequest object with the file name,
    // and sends it to the system through a socket.
    // Then, it waits for a response from the system and prints it
    private static void sendData(String fname) throws IOException, ClassNotFoundException {
        var req = new XMLProcessingRequest(fname);
        System.err.println("Mock User read xml");

        Session[] ses = new Session[1];
        writeReadRequest(req, ses);
        System.err.println("Mock client received " + ses[0]);
    }

    //takes a username as an argument, creates a UserDataRequest object with the username,
    // and sends it to the system through a socket.
    // Then, it waits for two responses from the system and prints them
    private static void requestData(String username) throws IOException, ClassNotFoundException {
        SessionMetadata[] smd = new SessionMetadata[2];
        writeReadRequest(new UserDataRequest(username), smd);
        for (var sd : smd) {
            System.err.println("Mock client received " + sd);
        }
    }

    //creates a socket to connect to the system, sends the request through an ObjectOutputStream, and waits for the response through an ObjectInputStream.
    // Once the response is received, it populates an array of objects passed as an argument with the response objects received from the system.
    private static void writeReadRequest(Request req, Object[] toReturn) throws IOException, ClassNotFoundException {
        Socket s = new Socket(Utilities.DISCOVERY_IP, Utilities.DISCOVERY_PORT);
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

        System.err.println("Mock User writing " + req);

        oos.writeObject(req);
        System.err.println("Mock User sent");

        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = ois.readObject();
        }
    }
}
