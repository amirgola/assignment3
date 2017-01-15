package bgu.spl171.net.api.bidi;

import bgu.spl171.net.srv.bidi.ConnectionHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Medhopz on 1/6/2017.
 */
public class ConnectionsImpl<T> implements Connections<T>{

    private ConcurrentHashMap<Integer,ConnectionHandler<T>> activeUsers;

    private int userId = 0; //maybe needs to be atomic
    private int waitingId = 0;
    public void ConnectionsImpl () {
        activeUsers = new ConcurrentHashMap<>();
    }

    public void addNewUser(ConnectionHandler<T> connection) {
        //every time we add a user he gets the next id
        activeUsers.put(Integer.valueOf(increaseId()), connection);
    }


    private int increaseId() {
        return userId++;
    }

    @Override
    public boolean send(int connectionId, T msg) {
        boolean result = false;
        ConnectionHandler<T> t = activeUsers.get(Integer.valueOf(connectionId));
        if(t != null) {
            //TO DO : implement connection handler send function
            t.send(msg);
            //if msg succesfull
            result = true;
        } else {
            //no such user
            result = false;
        }


        return result;
    }

    @Override
    public void broadcast(T msg) {

        //iterate through the hash and send a msg to all connections
        for (Integer i : activeUsers.keySet()) {
            this.send(Integer.valueOf(i), msg);
        }

    }

    @Override
    public void disconnect(int connectionId) {
        try {
            activeUsers.get(connectionId).close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        activeUsers.remove(Integer.valueOf(connectionId));
    }
}
