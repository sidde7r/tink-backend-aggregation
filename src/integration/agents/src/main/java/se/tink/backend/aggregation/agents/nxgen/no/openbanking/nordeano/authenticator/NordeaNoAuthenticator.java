package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordeano.authenticator;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordeano.NordeaNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordeano.NordeaNoConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaNoAuthenticator extends NordeaBaseAuthenticator {

    public NordeaNoAuthenticator(
            NordeaNoApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        super(apiClient, sessionStorage, persistentStorage);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, NordeaNoConstants.QueryValues.COUNTRY);
    }
}
