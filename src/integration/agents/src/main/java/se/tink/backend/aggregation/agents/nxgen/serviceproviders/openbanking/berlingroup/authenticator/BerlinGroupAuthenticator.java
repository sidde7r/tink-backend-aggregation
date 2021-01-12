package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
public abstract class BerlinGroupAuthenticator implements OAuth2Authenticator {

    protected final BerlinGroupApiClient apiClient;

    public BerlinGroupAuthenticator(final BerlinGroupApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(final String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token refreshAccessToken(final String refreshToken) throws BankServiceException {
        log.info("Token for refresh hash: {}}", refreshToken.hashCode());

        final OAuth2Token token = apiClient.refreshToken(refreshToken);
        apiClient.setTokenToSession(token, StorageKeys.OAUTH_TOKEN);

        token.getRefreshToken()
                .ifPresent(
                        refreshedToken ->
                                log.info("Refreshed token hash: {}", refreshedToken.hashCode()));

        return token;
    }

    @Override
    public void useAccessToken(final OAuth2Token accessToken) {
        apiClient.setTokenToSession(accessToken, StorageKeys.OAUTH_TOKEN);
    }

    public abstract OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException;
}
