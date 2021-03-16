package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.AuthorizeRequest.AuthorizeRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NordeaSeRedirectAuthenticator extends NordeaBaseAuthenticator {

    public NordeaSeRedirectAuthenticator(NordeaSeApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(
                new AuthorizeRequestBuilder()
                        .withCountry(BodyValues.COUNTRY)
                        .withState(state)
                        .withAuthenticationMethod(BodyValues.AUTHENTICATION_METHOD));
    }
}
