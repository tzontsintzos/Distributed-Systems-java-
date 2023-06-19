package src.gpx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//this class is designed to be thread-safe, to allow multiple threads
//to modify and read the segment and metadata fields in a synchronized manner

//instances of the class can be serialized (converted to a byte stream)
//and deserialized (converted back to an instance) for storage or transmission
public class Session implements Serializable {
    private final String user;
    private final ArrayList<Waypoint> segment = new ArrayList<>();  //contains the waypoints (GPS coordinates) recorded during the session
    private SessionMetadata metadata;  //summary information about the session

    //used to synchronize access to the metadata and segment fields, respectively,
    // in a thread-safe manner  (not serialized)
    private transient Object metadataLock = new Object();
    private transient Object segmentLock = new Object();

    public Session(String user) {
        this.user = user;
    }

    public Session(String user, List<Waypoint> segment) {
        this.user = user;

        for (var w : segment) {
            addWaypoint(w);
        }
    }

    //adds a new Waypoint to the segment list
    //checks if the segmentLock field is null (no other threads have accessed the field yet),
    //creates a new segmentLock object if it is.
    //The method then synchronizes on the segmentLock object to prevent multiple threads from modifying the segment list concurrently.
    public void addWaypoint(Waypoint w) {
        if (segmentLock == null) {
            synchronized (this) {
                segmentLock = new Object();
            }
        }
        synchronized (segmentLock) {  //only one thread per moment
            segment.add(w);
        }
    }

    //returns a shallow copy of the segment list
    //checks if the segmentLock field is null and creates a new segmentLock object if it is
    //then synchronizes on the segmentLock object to prevent other threads from modifying the list while the copy is being made
    public ArrayList<Waypoint> getSegment() {
        if (segmentLock == null) {
            synchronized (this) {
                segmentLock = new Object();
            }
        }
        synchronized (segmentLock) {
            // shallow copy; waypoint class is immutable, and by extension the entire contents of the list are so as well
            return new ArrayList<>(segment);
        }
    }

    //splits the segment list into segments equal-sized sublists,
    //returns an array of Session objects representing each sublist
    public Session[] splitSessionsEqually(int segments) {
        if (segmentLock == null) {
            synchronized (this) {
                segmentLock = new Object();
            }
        }
        synchronized (segmentLock) {
            int i = 0;
            Session[] subsessions = new Session[segments];

            // https://codereview.stackexchange.com/a/27930
            int size = (int) Math.ceil((float) segment.size() / segments);   //round robin
            for (int start = 0; start < segment.size(); start += size) {
                int end = Math.min(start + size, segment.size());
                subsessions[i++] = new Session(user, segment.subList(start, end));
            }

            return subsessions;
        }
    }

    public String getUser() {
        return user;
    }


    //used by the master to retrieve the summary information for the session
    public SessionMetadata getMetadata() {
        if (metadataLock == null) {
            synchronized (this) {
                metadataLock = new Object();
            }
        }
        synchronized (metadataLock) {
            return metadata;
        }
    }

    //used by the worker to calculate the summary information for the session
    public void setMetadata(SessionMetadata metadata) {
        if (metadataLock == null) {
            synchronized (this) {
                metadataLock = new Object();
            }
        }
        synchronized (metadataLock) {
            this.metadata = metadata;
        }
    }

    @Override
    public String toString() {
        return "Session{" +
                "user='" + user + '\'' +
                ", metadata=" + metadata +
                ", metadataLock=" + metadataLock +
                ", segment=" + segment +
                ", segmentLock=" + segmentLock +
                '}';
    }
}
