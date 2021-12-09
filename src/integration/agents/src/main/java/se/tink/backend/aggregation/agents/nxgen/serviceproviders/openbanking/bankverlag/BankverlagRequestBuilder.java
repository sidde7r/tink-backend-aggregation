package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.PathVariables;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class BankverlagRequestBuilder {

    private final TinkHttpClient client;
    private final RandomValueGenerator randomValueGenerator;
    private final String userIp;
    private final BankverlagHeaderValues headerValues;

    public RequestBuilder createRequest(URL url) {
        return createRequest(url, MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON_TYPE);
    }

    public RequestBuilder createRequest(URL url, MediaType acceptHeader, MediaType type) {
        url = getAspsUrl(url);

        return client.request(url)
                .type(type)
                .accept(acceptHeader)
                .header(Psd2Headers.Keys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .header(Psd2Headers.Keys.PSU_IP_ADDRESS, userIp);
    }

    private URL getAspsUrl(URL url) {
        if (url.get().contains("{" + PathVariables.ASPSP_ID + "}")) {
            return url.parameter(PathVariables.ASPSP_ID, headerValues.getAspspId());
        }

        return url;
    }
}
