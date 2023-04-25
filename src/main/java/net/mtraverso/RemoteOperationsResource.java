package net.mtraverso;

import com.google.inject.Inject;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class RemoteOperationsResource {

    RemoteOperationsManager remoteOperationsManager;
    SmsManager smsManager;

    @Inject
    public RemoteOperationsResource(RemoteOperationsManager remoteOperationsManager, SmsManager smsManager) {
        this.remoteOperationsManager = remoteOperationsManager;
        this.smsManager = smsManager;
    }

    @POST
    @Path("/subscribe")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // "hash" as a url parameter
    public Response subscribe(@FormParam("hash") String uuid) {
        Listener device = remoteOperationsManager.checkIn(uuid);
        boolean shouldOpenSite = device.shouldOpenSite();

        return Response.ok().header("shouldOpenSite", shouldOpenSite).build();
    }

    @POST
    @Path("/consume")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // "hash" as a url parameter
    public Response consume(@FormParam("hash") String uuid) {
        remoteOperationsManager.consume(uuid);
        return Response.ok().build();
    }

    @POST
    @Path("/sms") //twilio sends a post request to this endpoint
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public Response sms(@FormParam("Body") String body, @FormParam("From") String from) {
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


}
