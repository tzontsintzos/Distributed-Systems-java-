package src.worker;

import src.gpx.Session;
import src.gpx.SessionMetadata;
import src.utils.Utilities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// used by a worker node to process a GPX session

class Handler implements Runnable {
    private final Socket masterSocket;
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;

    public Handler(Socket s) throws IOException {
        masterSocket = s;

        ois = new ObjectInputStream(masterSocket.getInputStream());    //read and write objects to and from the Socket, respectively
        oos = new ObjectOutputStream(masterSocket.getOutputStream());
    }

    //the worker node reads a Session object from the ObjectInputStream,
    //which contains the data for the GPX session that needs to be processed.
    @Override
    public void run() {
        System.err.println("Worker running job " + Thread.currentThread().getName());
        try (masterSocket) {

            Session subsession = (Session) ois.readObject();
            SessionMetadata metadata = Utilities.calculateMetrics(subsession);
            oos.writeObject(metadata);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
