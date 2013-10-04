package services;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.client.ClientSessionChannel.MessageListener;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SfdcStreamer {

    public static interface Printer {
        void println(String s);
    }

    private static final String CHANNEL = "/topic/" + System.getenv("SFDC_PUSH_TOPIC");
    private static final String STREAMING_ENDPOINT_URI = "/cometd/28.0";
    private static final int CONNECTION_TIMEOUT = 20 * 1000;  // milliseconds
    private static final int READ_TIMEOUT = 120 * 1000; // milliseconds

    final BayeuxClient client = makeClient();

    public void start(final Printer out) {
        client.handshake();
        out.println("Waiting for handshake");

        boolean handshaken = client.waitFor(10 * 1000, BayeuxClient.State.CONNECTED);
        if (!handshaken) {
            out.println("Failed to handshake: " + client);
        }

        out.println("Subscribing for channel: " + CHANNEL);

        client.getChannel(CHANNEL).subscribe(new MessageListener() {
            @Override
            public void onMessage(ClientSessionChannel channel, Message message) {
                out.println("Received Message: " + message);
            }
        });
    }

    public void stop() {
        client.disconnect();
    }

    private BayeuxClient makeClient()  {
        HttpClient httpClient = new HttpClient();
        httpClient.setConnectTimeout(CONNECTION_TIMEOUT);
        httpClient.setTimeout(READ_TIMEOUT);
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final String sessionid = System.getenv("SFDC_SESSION_ID");
        final String endpoint = System.getenv("SFDC_ENDPOINT");

        Map<String, Object> options = new HashMap<String, Object>();
        options.put(ClientTransport.TIMEOUT_OPTION, READ_TIMEOUT);
        LongPollingTransport transport = new LongPollingTransport(options, httpClient) {
            @Override
            protected void customize(ContentExchange exchange) {
                super.customize(exchange);
                exchange.addRequestHeader("Authorization", "OAuth " + sessionid);
            }
        };

        try {
            return new BayeuxClient(new URL(endpoint + STREAMING_ENDPOINT_URI).toExternalForm(), transport);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

