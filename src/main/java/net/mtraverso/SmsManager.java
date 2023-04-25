package net.mtraverso;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SmsManager {

    private enum Command {
        AUTHORIZE_NUMBER("auth"),
        DEAUTHORIZE_NUMBER("deauth"),
        LIST_DEVICES("list"),
        DEVICE_OPEN_SITE("nuke"),
        ALIAS_DEVICE("alias"),
        ALIAS_DEVICE_BY_NUMBER("aliasn"),
        SET_DEVICE_REDIRECT("redirect");

        Command(String command_string) {
        }

        static Command of(String command_string) {
            for (Command command : Command.values()) { //search for the equal command_string in the enum
                if (command.name().equals(command_string.toLowerCase())) {
                    return command;
                }
            }
            return null;
        }
    }

    List<String> authorizedNumbers;
    RemoteOperationsManager remoteOperationsManager;

    URL redirectUrl;

    @Inject
    public SmsManager(RemoteOperationsManager remoteOperationsManager) throws MalformedURLException {
        this.remoteOperationsManager = remoteOperationsManager;
        this.redirectUrl = new URL("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        this.authorizedNumbers = List.of("+6503398121");
    }

    public void addAuthorizedNumber(String number) {
        authorizedNumbers.add(number);
    }

    public void removeAuthorizedNumber(String number) {
        authorizedNumbers.remove(number);
    }

    public boolean isAuthorizedNumber(String number) {
        return authorizedNumbers.contains(number);
    }

    public String handleSms(String from, String body) {
        if (!isAuthorizedNumber(from)) {
            return "Unauthorized number";
        }
        String[] split = body.split(" ");
        Command command = Command.of(split[0]);
        if (command == null) {
            return "Invalid command";
        }

        return switch (command) {
            case AUTHORIZE_NUMBER -> {
                addAuthorizedNumber(split[1]);
                yield "Authorized number: " + split[1];
            }
            case DEAUTHORIZE_NUMBER -> {
                removeAuthorizedNumber(split[1]);
                yield "Deauthorized number: " + split[1];
            }
            case LIST_DEVICES -> {
                //get only the listeners that are alive
                ListIterator<Listener> iterator = (ListIterator<Listener>) remoteOperationsManager.getListeners().stream().filter(Listener::isAlive).iterator();

                StringBuilder sb = new StringBuilder();
                while (iterator.hasNext()) {
                    Listener listener = iterator.next();
                    sb.append(iterator.nextIndex()).append(": ").append(listener.hasName() ? listener.getName() : listener.getUUID());
                    if (iterator.hasNext()) {
                        sb.append("\n");
                    }
                }
                yield sb.toString();
            }
            case DEVICE_OPEN_SITE -> {
                Listener listener = Listener.getListenerByEither(remoteOperationsManager.getListeners(), split[1]);
                if (listener != null) {
                    listener.setShouldOpenSite(true);
                }
                yield listener != null ? "Successfully set " + listener.getName() + " to open site" : "No listener found";

            }
            case ALIAS_DEVICE -> {
                Listener listener = Listener.getListenerByEither(remoteOperationsManager.getListeners(), split[1]);
                if (listener != null) {
                    listener.setName(split[2]);
                }
                yield listener != null ? "Successfully aliased " + listener.getUUID() + " to " + listener.getName() : "No listener found";
            }
            case ALIAS_DEVICE_BY_NUMBER -> {
                Iterator<Listener> iterator = remoteOperationsManager.getListeners()
                        .stream()
                        .filter(Listener::isAlive)
                        .iterator();

                int i = 0;
                while (iterator.hasNext()) {
                    Listener listener = iterator.next();
                    if (i == Integer.parseInt(split[1])) {
                        listener.setName(split[2]);
                        break;
                    }
                }
                yield "Successfully aliased " + split[1] + " to " + split[2];

            }
            case SET_DEVICE_REDIRECT -> {
                try {
                    this.redirectUrl = new URL(split[1]);
                    yield "Successfully set redirect URL to " + split[1];
                } catch (MalformedURLException e) {
                    yield "Invalid URL";
                }
            }
        };

    }
}
