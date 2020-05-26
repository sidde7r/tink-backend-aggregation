package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.authenticator;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NordeaFiAuthenticator extends NordeaBaseAuthenticator {

    private static final String COUNTRY = "FI";

    public NordeaFiAuthenticator(NordeaFiApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, COUNTRY);
    }
}
