package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.authenticator;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.NordeaNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.NordeaNoConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.AuthorizeRequest.AuthorizeRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NordeaNoAuthenticator extends NordeaBaseAuthenticator {

    public NordeaNoAuthenticator(NordeaNoApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(
                new AuthorizeRequestBuilder()
                        .withCountry(NordeaNoConstants.QueryValues.COUNTRY)
                        .withState(state)
                        .withSkipAccountSelection(true));
    }
}
