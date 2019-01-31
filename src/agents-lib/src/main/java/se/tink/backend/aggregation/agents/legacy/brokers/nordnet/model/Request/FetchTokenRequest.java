package se.tink.backend.aggregation.agents.brokers.nordnet.model.Request;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class FetchTokenRequest extends MultivaluedMapImpl {
    private FetchTokenRequest() {
        this.add("client_id", "MOBILE_IOS");
        this.add("grant_type", "authorization_code");
        this.add("redirect_uri", "https://www.nordnet.se/now/mobile/token.html");
    }

    public static FetchTokenRequest from(String clientSecret, String code) {
        FetchTokenRequest request = new FetchTokenRequest();
        request.add("client_secret", clientSecret);
        request.add("code", code);

        return request;
    }
}
