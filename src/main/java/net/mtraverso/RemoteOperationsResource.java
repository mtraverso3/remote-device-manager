package net.mtraverso;

import com.google.inject.Inject;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;
import io.airlift.log.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

@Path("/")
public class RemoteOperationsResource {

    RemoteOperationsManager remoteOperationsManager;
    SmsManager smsManager;
    Logger logger = Logger.get(RemoteOperationsResource.class);

    @Inject
    public RemoteOperationsResource(RemoteOperationsManager remoteOperationsManager, SmsManager smsManager) {
        this.remoteOperationsManager = remoteOperationsManager;
        this.smsManager = smsManager;
    }

    @POST
    @Path("/subscribe")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // "hash" as a url parameter
    public Response subscribe(@QueryParam("hash") String uuid) {
        Listener device = remoteOperationsManager.checkIn(uuid);
        boolean shouldOpenSite = device.shouldOpenSite();
        logger.info("Subscribed: " + uuid + " shouldOpenSite: " + shouldOpenSite);
//        return a json object with the following fields: data=shouldOpenSite
        return Response.ok().entity("{\"data\":" + shouldOpenSite + "}").build();
//        return Response.ok().header("data", shouldOpenSite).build();
    }

    @POST
    @Path("/consume")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // "hash" as a url parameter
    public Response consume(@QueryParam("hash") String uuid) {
        logger.info("Consumed: " + uuid);
        remoteOperationsManager.consume(uuid);
        return Response.ok().build();
    }

    @POST
    @Path("/sms") //twilio sends a post request to this endpoint
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public Response sms(@FormParam("Body") String body, @FormParam("From") String from) {
        logger.info("Received SMS from " + from + ": " + body);
        String responseString = smsManager.handleSms(from, body);
        Body responseBody = new Body
                .Builder(responseString)
                .build();
        Message sms = new Message
                .Builder()
                .body(responseBody)
                .build();
        MessagingResponse twiml = new MessagingResponse
                .Builder()
                .message(sms)
                .build();
        return Response.ok(twiml.toXml()).build();
    }

    @GET
    @Path("/location")
    public Response location(){
        try {
            return Response.temporaryRedirect(smsManager.redirectUrl.toURI()).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }



}
