package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.filter.UkOpenBankingPisRequestFilter;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class UkOpenBankingRequestBuilder {

    private final TinkHttpClient httpClient;
    private final UkOpenBankingPisRequestFilter pisRequestFilter;

    public RequestBuilder createPisRequest(URL url) {
        return createRequest(url).addFilter(pisRequestFilter);
    }

    public RequestBuilder createPisRequestWithJwsHeader(URL url) {
        return createRequest(url).addFilter(pisRequestFilter).type(MediaType.APPLICATION_JSON_TYPE);
    }

    public RequestBuilder createRequest(URL url) {
        return httpClient.request(url).accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
