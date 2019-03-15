package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordeafi.authenticator;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordeafi.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordeafi.NordeaFiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaFiAuthenticator extends NordeaBaseAuthenticator {

    public NordeaFiAuthenticator(
            NordeaFiApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        super(apiClient, sessionStorage, persistentStorage);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, NordeaFiConstants.QueryValues.COUNTRY);
    }
}
