package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea.authenticator;

import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaDkAuthenticator extends NordeaBaseAuthenticator {

    public NordeaDkAuthenticator(
            NordeaDkApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        super(apiClient, sessionStorage, persistentStorage);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, NordeaDkConstants.QueryValues.COUNTRY);
    }
}
