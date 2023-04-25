package net.mtraverso;

import java.util.ArrayList;
import java.util.List;

public class RemoteOperationsManager {

    private final List<Listener> listeners;

    RemoteOperationsManager() {
        this.listeners = new ArrayList<>();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public List<Listener> getListeners() {
        return listeners;
    }

    synchronized public Listener checkIn(String uuid) {
        Listener listener = Listener.getListenerByUUID(listeners, uuid);
        if (listener != null) {
            listener.checkIn();
        } else {
            listener = new Listener(uuid);
            listeners.add(listener);
        }
        return listener;
    }

    public void consume(String UUID) {
        Listener listener = Listener.getListenerByUUID(listeners, UUID);
        if (listener != null) {
            listener.checkIn();
            listener.setShouldOpenSite(false);
        }
    }





}
