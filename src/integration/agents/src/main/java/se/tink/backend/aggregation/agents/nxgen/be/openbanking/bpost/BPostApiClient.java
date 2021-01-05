package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@RequiredArgsConstructor
public class BPostApiClient {

    private final TinkHttpClient client;

    public String getAuthorizationEndpoint(String authorizationEndpointSource) {
        return client.request(authorizationEndpointSource)
                .get(WellKnownResponse.class)
                .getAuthorizationEndpoint();
    }
}
