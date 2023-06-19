package src.master;

import src.utils.SimpleQueue;
import src.utils.Utilities;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

//listens for user connections and worker connections on different sockets. It accepts a user connection,
//then waits to accept a specific number of worker connections before starting a job using the accepted user and worker sockets.

public class Master {

    private final int workerCount;
    private final InetAddress bCastAddress = InetAddress.getByName(Utilities.DISCOVERY_IP);
    private final InetAddress localhostAddress = InetAddress.getByName(Utilities.LOCAL_DISCOVERY_IP);
    private final ServerSocket userServerSocket;
    private final ServerSocket localServerSocket;

    public Master(int workerCount) throws IOException {
        this.workerCount = workerCount;

        userServerSocket = new ServerSocket(Utilities.DISCOVERY_PORT, 1000, bCastAddress);
        localServerSocket = new ServerSocket(Utilities.LOCAL_DISCOVERY_PORT, workerCount, localhostAddress);
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException {
        Master master = new Master(Integer.parseInt(args[0]));
        master.run();
    }

    //starts the master and listens for incoming requests from users and workers.
    // For each user request it accepts a connection and passes the request to the available worker.
    public void run() throws IOException, ParserConfigurationException {
        while (true) {
            // this is blocking, which means workers will not be able to create a new connection
            //  without us having received a new job from a user first
            startJob(acceptUser(), acceptWorkers());
        }
    }

    //starts a new job by creating a new Handler thread that will handle communication between the user and the workers.
    private void startJob(final Socket user, final Queue<Socket> workers) throws IOException, ParserConfigurationException {
        (new Thread(new Handler(user, workers))).start();   //thread starts
    }

    //accepts a user connection and returns the corresponding socket.
    private Socket acceptUser() throws IOException {
        System.err.println("Master waiting for users to serve...");
        return userServerSocket.accept();
    }

    //accepts connections from the available workers and returns a queue of worker sockets.
    private Queue<Socket> acceptWorkers() throws IOException {
        final Queue<Socket> workers = new SimpleQueue<>();

        System.err.println("Master waiting to accept " + workerCount + " workers...");
        for (int i = 0; i < workerCount; ++i) {
            workers.add(localServerSocket.accept());
            System.err.println("Master accepted worker on port " + localServerSocket.getLocalPort());
        }

        // after initializing all workers, do not close the server socket
        //  as it'll be needed to setup next queue of sessions
        return workers;
    }
}
