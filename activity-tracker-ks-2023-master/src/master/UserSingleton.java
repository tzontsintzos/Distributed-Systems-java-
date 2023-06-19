package src.master;

import src.gpx.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//for bonus

//implementation of the Singleton design pattern for a User object

//maintains a single instance of UserSingleton that can be accessed via the getInstance() method.
//The class also provides methods for adding new User objects to its collection of users,
//retrieving a list of sessions for a specific user, and retrieving a list of all users.
public class UserSingleton {
    private static final Object _instance_lock = new Object();
    private static UserSingleton _instance = null;
    private final HashMap<String, User> userData = new HashMap<>();  //store the User objects
    private final Object userDataLock = new Object();

    private UserSingleton() {

    }

    public static UserSingleton getInstance() {
        if (_instance == null) {
            synchronized (_instance_lock) {
                _instance = new UserSingleton();
            }
        }

        return _instance;
    }

    public void addUser(String u) {
        synchronized (userDataLock) {
            userData.computeIfAbsent(u, k -> new User());
        }
    }

    //add single session to a user
    public void addUser(String u, Session s) {
        synchronized (userDataLock) {
            if (userData.get(u) == null) {
                userData.put(u, new User(s));
                return;
            }

            userData.get(u).addSession(s);
        }
    }

    //add multiple sessions to a user
    public void addUser(String u, ArrayList<Session> sessions) {
        synchronized (userDataLock) {
            if (userData.get(u) == null) {
                userData.put(u, new User(sessions));
                return;
            }

            userData.get(u).addSessions(sessions);
        }
    }

    //returns a List of Session objects for the specified user.
    // If the user does not exist in the HashMap, an empty List is returned.
    public List<Session> getUserSessions(String u) {
        synchronized (userDataLock) {
            return userData.get(u).getSessions();
        }
    }

    //returns a List of all the keys (i.e. usernames) in the HashMap
    public List<String> keyList() {
        synchronized (userDataLock) {
            return new ArrayList<>(userData.keySet());
        }
    }
}
