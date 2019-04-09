package se.tink.libraries.net;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import java.util.Map;

public class LightweightHttpRequestFactory {

    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 30000;

    private static Builder createBuilder(String url) {
        Client client = Client.create();
        client.setConnectTimeout(CONNECT_TIMEOUT_MS);
        client.setReadTimeout(READ_TIMEOUT_MS);
        WebResource webResource = client.resource(url);
        Builder builder = webResource.accept("application/json");
        return builder;
    }

    public static LightweightHttpRequest create(String url) {
        return new LightweightHttpRequest(createBuilder(url));
    }

    public static LightweightHttpRequest create(String url, Map<String, String> headers) {
        Builder builder = createBuilder(url);
        for (String key : headers.keySet()) {
            builder = builder.header(key, headers.get(key));
        }

        return new LightweightHttpRequest(builder);
    }
}
