package net.mtraverso;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class Listener {

    public static final int ALIVE_TIMEOUT = 10_000; //milliseconds

    private String name = null;
    private final String uuid;
    private final Timestamp lastCheckIn;

    private boolean shouldOpenSite = false;
    private URI redirectURI = URI.create("https://www.youtube.com/watch?v=dQw4w9WgXcQ");

    @JsonCreator
    Listener(@JsonProperty("uuid") String uuid) {
        this.uuid = uuid;
        this.lastCheckIn = new Timestamp(System.currentTimeMillis());
    }
    @JsonCreator
    Listener(@JsonProperty("name") String name, @JsonProperty("uuid") String uuid) {
        this.name = name;
        this.uuid = uuid;
        this.lastCheckIn = new Timestamp(System.currentTimeMillis());
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public boolean hasName() {
        return name != null;
    }

    @JsonProperty
    public String getUUID() {
        return uuid;
    }

    @JsonProperty
    public URI getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(URI redirectURI) {
        this.redirectURI = redirectURI;
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

    public static Optional<Listener> getListenerByUUID(List<Listener> listeners, String uuid) {
        for (Listener listener : listeners) {
            if (listener.getUUID().equals(uuid)) {
                return Optional.of(listener);
            }
        }
        return Optional.empty();
    }

    public static Optional<Listener> getListenerByName(List<Listener> listeners, String name) {
        for (Listener listener : listeners) {
            if (listener.getName().equals(name)) {
                return Optional.of(listener);
            }
        }
        return Optional.empty();
    }

    public static Optional<Listener> getListenerByEither(List<Listener> listeners, String nameOrUUID) {
        Optional<Listener> listener = getListenerByUUID(listeners, nameOrUUID);
        if (listener.isEmpty()) {
            listener = getListenerByName(listeners, nameOrUUID);
        }
        return listener;
    }


}
