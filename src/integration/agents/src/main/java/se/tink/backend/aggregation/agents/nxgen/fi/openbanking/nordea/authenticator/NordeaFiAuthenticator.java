package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.authenticator;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.NordeaFiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaFiAuthenticator extends NordeaBaseAuthenticator {

    public NordeaFiAuthenticator(NordeaFiApiClient apiClient, SessionStorage sessionStorage) {
        super(apiClient, sessionStorage);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, NordeaFiConstants.QueryValues.COUNTRY);
    }
}
