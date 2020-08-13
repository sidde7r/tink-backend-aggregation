package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class DnbAuthenticator {

    private final DnbApiClient apiClient;

    public DnbAuthenticator(final DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(final String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    public boolean isConsentValid() {
        return apiClient.isConsentValid();
    }
}
