package net.mtraverso;

import io.airlift.bootstrap.Bootstrap;
import io.airlift.event.client.EventModule;
import io.airlift.http.server.HttpServerModule;
import io.airlift.jaxrs.JaxrsModule;
import io.airlift.json.JsonModule;
import io.airlift.node.NodeModule;

public class Main {
    public static void main(String[] args) {
        Bootstrap app = new Bootstrap(
                new NodeModule(),
                new HttpServerModule(),
                new EventModule(),
                new JsonModule(),
                new JaxrsModule(),
                new RemoteOperationsModule()
        );

        app.initialize();
    }
}