package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

// TODO: Authentication must be implemented for the production
public class BecAuthenticator implements OAuth2Authenticator {
    private final BecApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BecAuthenticator(BecApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        ConsentResponse response = apiClient.getConsent(apiClient.createConsentRequestBody());
        return new URL(response.getScaRedirect());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return null;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
        throws SessionException, BankServiceException {
        return null;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {

    }
}
