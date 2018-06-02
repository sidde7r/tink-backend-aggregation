package se.tink.backend.connector.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

public class ClientUtils {

    private static final int READ_TIMEOUT_MS = 30000;
    private static final int CONNECT_TIMEOUT_MS = 10000;

    public static Client createBasicClient() {
        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();

        Client client = Client.create(clientConfig);
        client.setChunkedEncodingSize(null);
        setTimeouts(client);

        return client;
    }

    private static void setTimeouts(Client client) {
        client.setReadTimeout(READ_TIMEOUT_MS);
        client.setConnectTimeout(CONNECT_TIMEOUT_MS);
    }
}
