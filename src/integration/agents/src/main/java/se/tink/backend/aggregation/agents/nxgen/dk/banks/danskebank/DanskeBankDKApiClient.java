package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class DanskeBankDKApiClient extends DanskeBankApiClient {
    protected DanskeBankDKApiClient(
            TinkHttpClient client, DanskeBankDKConfiguration configuration) {
        super(client, configuration);
    }

    @Override
    public HttpResponse collectDynamicLogonJavascript(String securitySystem, String brand) {
        try {
            return super.collectDynamicLogonJavascript(securitySystem, brand);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();

            if (response.getStatus() == 412
                    && response.getBody(String.class).equalsIgnoreCase("Unauthorized client")) {
                throw new IllegalStateException(
                        "Check if DanskebankDK has rotated their clientSecret", e);
            }
            throw e;
        }
    }
}
