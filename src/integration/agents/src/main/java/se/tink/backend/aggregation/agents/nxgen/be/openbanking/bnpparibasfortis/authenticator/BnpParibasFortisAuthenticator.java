package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.http.BnpParibasFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BnpParibasFortisAuthenticator implements OAuth2Authenticator {

    private final BnpParibasFortisApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BnpParibasFortisAuthenticator(
            BnpParibasFortisApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return apiClient.getToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        final OAuth2Token accessToken = apiClient.refreshToken(refreshToken);
        sessionStorage.put(BnpParibasFortisConstants.StorageKeys.OAUTH_TOKEN, accessToken);
        return accessToken;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(BnpParibasFortisConstants.StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
