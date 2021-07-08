package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaStorage;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class SpardaRequestBuilder {

    private final TinkHttpClient client;
    private final RandomValueGenerator randomValueGenerator;
    private final String userIp;
    private final SpardaStorage storage;

    RequestBuilder createRawRequest(URL url) {
        return client.request(url);
    }

    RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(Psd2Headers.Keys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .header(Psd2Headers.Keys.PSU_IP_ADDRESS, userIp);
    }

    RequestBuilder createRequestInSession(URL url) {
        return createRequest(url).addBearerToken(storage.getToken());
    }

    RequestBuilder createRequestWithConsent(URL url) {
        return createRequestInSession(url)
                .header(Psd2Headers.Keys.CONSENT_ID, storage.getConsentId());
    }
}
