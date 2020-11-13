package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank;

import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.i18n.Catalog;

public class DanskeBankNOApiClient extends DanskeBankApiClient {
    protected DanskeBankNOApiClient(
            TinkHttpClient client,
            DanskeBankNOConfiguration configuration,
            Credentials credentials,
            Catalog catalog) {
        super(client, configuration, credentials, catalog);

        client.setUserAgent(configuration.getUserAgent());
    }

    public HttpResponse getNoBankIdDynamicJs(String securitySystem) {
        return client.request(
                        String.format(
                                constants.getBaseUrl()
                                        + DanskeBankNOConstants.BANKID_DYNAMIC_JS_URL,
                                securitySystem))
                .header("Referer", configuration.getAppReferer())
                .type(MediaType.TEXT_PLAIN_TYPE)
                .accept(MediaType.WILDCARD)
                .header("Origin", DanskeBankNOConstants.HTTP_ORIGIN)
                .get(HttpResponse.class);
    }
}
