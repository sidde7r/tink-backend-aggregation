package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.i18n.Catalog;

public class DanskeBankDKApiClient extends DanskeBankApiClient {
    DanskeBankDKApiClient(
            TinkHttpClient client,
            DanskeBankDKConfiguration configuration,
            Credentials credentials,
            Catalog catalog) {
        super(client, configuration, credentials, catalog);
        client.setUserAgent(configuration.getUserAgent());
    }

    @Override
    public HttpResponse collectDynamicLogonJavascript(String securitySystem, String brand) {
        try {
            return super.collectDynamicLogonJavascript(securitySystem, brand);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() == 412
                    && StringUtils.containsIgnoreCase(
                            response.getBody(String.class), "unauthorized client")) {
                throw new IllegalStateException(
                        "Check if DanskebankDK has rotated their clientSecret", e);
            }
            throw e;
        }
    }
}
