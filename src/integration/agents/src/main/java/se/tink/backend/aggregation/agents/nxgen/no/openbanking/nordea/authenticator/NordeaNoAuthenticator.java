package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.authenticator;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.NordeaNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.NordeaNoConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaNoAuthenticator extends NordeaBaseAuthenticator {

    public NordeaNoAuthenticator(NordeaNoApiClient apiClient, SessionStorage sessionStorage) {
        super(apiClient, sessionStorage);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, NordeaNoConstants.QueryValues.COUNTRY);
    }
}
