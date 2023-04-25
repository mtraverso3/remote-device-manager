package net.mtraverso;

import java.sql.Timestamp;
import java.util.List;

public class Listener {

    public static final int ALIVE_TIMEOUT = 10_000;

    private String name = null;
    private final String uuid;
    private Timestamp lastCheckIn;

    private boolean shouldOpenSite = false;

    Listener(String UUID) {
        this.uuid = UUID;
        this.lastCheckIn = new Timestamp(System.currentTimeMillis());
    }
    Listener(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
        this.lastCheckIn = new Timestamp(System.currentTimeMillis());
    }

    public String getName() {
        return name;
    }

    public boolean hasName() {
        return name != null;
    }

    public String getUUID() {
        return uuid;
    }

    public Timestamp getLastCheckIn() {
        return lastCheckIn;
    }

    public void checkIn() {
        this.lastCheckIn.setTime(System.currentTimeMillis());
    }

    public boolean isAlive() {
        return System.currentTimeMillis() - lastCheckIn.getTime() < ALIVE_TIMEOUT;
    }

    public boolean shouldOpenSite() {
        return shouldOpenSite;
    }

    /**
     * Only opens if the listener is alive
     * @param shouldOpenSite
     */
    public void setShouldOpenSite(boolean shouldOpenSite) {
        this.shouldOpenSite = shouldOpenSite && isAlive();
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Listener getListenerByUUID(List<Listener> listeners, String uuid) {
        for (Listener listener : listeners) {
            if (listener.getUUID().equals(uuid)) {
                return listener;
            }
        }
        return null;
    }

    public static Listener getListenerByName(List<Listener> listeners, String name) {
        for (Listener listener : listeners) {
            if (listener.getName().equals(name)) {
                return listener;
            }
        }
        return null;
    }

    public static Listener getListenerByEither(List<Listener> listeners, String nameOrUUID) {
        Listener listener = getListenerByUUID(listeners, nameOrUUID);
        if (listener == null) {
            listener = getListenerByName(listeners, nameOrUUID);
        }
        return listener;
    }


}
