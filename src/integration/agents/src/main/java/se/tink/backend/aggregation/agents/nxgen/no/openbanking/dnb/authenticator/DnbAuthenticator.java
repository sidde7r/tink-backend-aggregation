package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public class DnbAuthenticator implements OAuth2Authenticator {

    private final DnbApiClient apiClient;

    public DnbAuthenticator(final DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(final String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) throws BankServiceException {
        return null;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {
        return null;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}
