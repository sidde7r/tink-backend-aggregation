package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.AuthorizeRequest.AuthorizeRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NordeaSeAuthenticator extends NordeaBaseAuthenticator {

    public NordeaSeAuthenticator(NordeaSeApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(
                new AuthorizeRequestBuilder()
                        .withCountry(NordeaSeConstants.Authentication.COUNTRY)
                        .withState(state)
                        .withAuthenticationMethod(
                                NordeaSeConstants.Authentication.AUTHENTICATION_METHOD));
    }
}
