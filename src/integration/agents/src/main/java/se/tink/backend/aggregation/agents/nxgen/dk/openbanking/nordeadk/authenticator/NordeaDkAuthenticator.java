package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordeadk.authenticator;

import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordeadk.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordeadk.NordeaDkConstants;
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
