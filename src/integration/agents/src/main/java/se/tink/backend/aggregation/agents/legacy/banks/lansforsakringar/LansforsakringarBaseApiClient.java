package se.tink.backend.aggregation.agents.banks.lansforsakringar;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import java.net.URI;
import java.util.function.Function;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import se.tink.libraries.net.client.TinkApacheHttpClient4;

public class LansforsakringarBaseApiClient {
    private final TinkApacheHttpClient4 client;
    private final Function<String, URI> uriFunction;
    private final String deviceId;

    public LansforsakringarBaseApiClient(
            TinkApacheHttpClient4 client, Function<String, URI> uriFunction, String deviceId) {
        this.client = client;
        this.uriFunction = uriFunction;
        this.deviceId = deviceId;
    }

    public Builder createClientRequest(
            String url, String token, String ticket, String userSession) {
        WebResource.Builder request =
                client.resource(uriFunction.apply(url)).accept(MediaType.APPLICATION_JSON);

        if (token != null) {
            request = request.header("ctoken", token);
        }

        if (ticket != null) {
            request = request.header("Utoken", ticket);
        }

        if (deviceId != null) {
            request = request.header("DeviceId", deviceId);
        }

        if (userSession != null) {
            request.cookie(new Cookie("USERSESSION", userSession));
        }

        return request;
    }
}
