package net.mtraverso;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        Optional<Listener> listener = Listener.getListenerByUUID(listeners, uuid);

        if (listener.isPresent()) {
            listener.orElseThrow().checkIn();
            return listener.orElseThrow();
        }

        Listener newListener = new Listener(uuid);
        listeners.add(newListener);
        return newListener;
    }

    public void consume(String UUID) {
        Optional<Listener> listener = Listener.getListenerByUUID(listeners, UUID);

        if (listener.isPresent()) {
            listener.orElseThrow().checkIn();
            listener.orElseThrow().setShouldOpenSite(false);
        }
    }
}
