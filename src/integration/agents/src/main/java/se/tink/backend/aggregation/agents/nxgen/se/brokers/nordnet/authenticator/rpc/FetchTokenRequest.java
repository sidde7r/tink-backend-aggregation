package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class FetchTokenRequest extends MultivaluedMapImpl {
    private FetchTokenRequest() {
        this.add("grant_type", "authorization_code");
        this.add("redirect_uri", "https://www.nordnet.se/now/mobile/token.html");
    }

    public static FetchTokenRequest from(String clientId, String clientSecret, String code) {
        FetchTokenRequest request = new FetchTokenRequest();
        request.add("client_id", clientId);
        request.add("client_secret", clientSecret);
        request.add("code", code);

        return request;
    }
}
