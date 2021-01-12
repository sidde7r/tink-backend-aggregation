package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@RequiredArgsConstructor
public class CommerzbankApiClient {
    private final TinkHttpClient client;

    public String getAuthorizationEndpoint(String authorizationEndpointSource) {
        return client.request(authorizationEndpointSource)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(WellKnownResponse.class)
                .getAuthorizationEndpoint();
    }
}
