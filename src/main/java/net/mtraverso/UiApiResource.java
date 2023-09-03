package net.mtraverso;

import io.airlift.log.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/")
public class UiApiResource {

    private final RemoteOperationsManager remoteOperationsManager;
    private final Logger logger = Logger.get(UiApiResource.class);

    @Inject
    public UiApiResource(RemoteOperationsManager remoteOperationsManager) {
        this.remoteOperationsManager = remoteOperationsManager;
    }

    @GET
    @Path("/listeners")
    public List<Listener> getListeners() {
        return remoteOperationsManager.getListeners().stream().filter(Listener::isAlive).toList();
    }

    @POST
    @Path("/alias")
    public void setAlias(@QueryParam("uuid") String uuid, @QueryParam("alias") String alias) {
        Optional<Listener> listener = Listener.getListenerByUUID(remoteOperationsManager.getListeners(), uuid);

        listener.ifPresent(value -> value.setName(alias));

        if (listener.isPresent()) {
            logger.info("Alias changed for " + uuid + " to " + alias);
        } else {
            logger.info("Alias change failed for " + uuid + " to " + alias);
        }
    }

    @POST
    @Path("/nuke")
    public void nuke(@QueryParam("uuid") String uuid) {
        Optional<Listener> listener = Listener.getListenerByUUID(remoteOperationsManager.getListeners(), uuid);

        listener.ifPresent(value -> value.setShouldOpenSite(true));

        if (listener.isPresent()) {
            logger.info("Nuke command sent to " + uuid);
        } else {
            logger.info("Nuke command failed to send to " + uuid);
        }
    }

    @GET
    @Path("/redirect")
    public Optional<String> getRedirect(@QueryParam("uuid") String uuid) {
        Optional<Listener> listener = Listener.getListenerByUUID(remoteOperationsManager.getListeners(), uuid);

        return listener.map(Listener::getRedirectURI).map(Object::toString);
    }

    @POST
    @Path("/redirect")
    public void setRedirect(@QueryParam("uuid") String uuid, @QueryParam("redirect") String redirect) {
        Optional<Listener> listener = Listener.getListenerByUUID(remoteOperationsManager.getListeners(), uuid);

        try {
            URI newRedirect = new URI(redirect);
            listener.ifPresent(value -> value.setRedirectURI(newRedirect));
        } catch (URISyntaxException e) {
            logger.info("Redirect change failed for " + uuid + " to " + redirect);
            return;
        }

        if (listener.isPresent()) {
            logger.info("Redirect changed for " + uuid + " to " + redirect);
        } else {
            logger.info("Redirect change failed for " + uuid + " to " + redirect);
        }
    }

}
