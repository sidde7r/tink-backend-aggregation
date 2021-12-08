package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.HeaderKeys;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class FabricRequestBuilder {

    private final TinkHttpClient client;
    private final RandomValueGenerator randomValueGenerator;
    private final String userIp;

    RequestBuilder createRequest(URL url) {
        RequestBuilder request = client.request(url);
        request.header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID());
        request.header(HeaderKeys.PSU_IP_ADDRESS, userIp);
        request.accept(MediaType.APPLICATION_JSON);
        request.type(MediaType.APPLICATION_JSON);
        return request;
    }

    RequestBuilder createRequestInSession(URL url, String consentId) {
        return createRequest(url).header(HeaderKeys.CONSENT_ID, consentId);
    }
}
