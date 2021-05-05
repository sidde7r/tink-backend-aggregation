package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea.authenticator;

import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.AuthorizeRequest.AuthorizeRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NordeaDkAuthenticator extends NordeaBaseAuthenticator {

    private static final String COUNTRY = "DK";

    public NordeaDkAuthenticator(NordeaDkApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(
                new AuthorizeRequestBuilder()
                        .withCountry(COUNTRY)
                        .withState(state)
                        .withSkipAccountSelection(true));
    }
}
