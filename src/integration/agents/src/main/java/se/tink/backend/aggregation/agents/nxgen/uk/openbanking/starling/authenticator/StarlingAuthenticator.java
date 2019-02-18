package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.CodeExchangeForm;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.TokenRefreshForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class StarlingAuthenticator implements OAuth2Authenticator {

    private final StarlingApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public StarlingAuthenticator(StarlingApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return new URL(StarlingConstants.Url.AUTH_STARLING)
                .queryParam(StarlingConstants.RequestKey.CLIENT_ID, this.getClientId())
                .queryParam(StarlingConstants.RequestKey.REDIRECT_URI, this.getRedirectUrl())
                .queryParam(
                        StarlingConstants.RequestKey.RESPONSE_TYPE,
                        StarlingConstants.RequestValue.CODE)
                .queryParam(StarlingConstants.RequestKey.STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {

        CodeExchangeForm exchangeForm =
                CodeExchangeForm.builder()
                        .withCode(code)
                        .asClient(getClientId(), getClientSecret())
                        .withRedirect(getRedirectUrl())
                        .build();

        return apiClient.exchangeCode(exchangeForm);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        TokenRefreshForm refreshForm =
                TokenRefreshForm.builder()
                        .withRefreshToken(getRefreshToken())
                        .asClient(getClientId(), getClientSecret())
                        .build();

        return apiClient.refreshAccessToken(refreshForm);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {/* Using persistent storage instead. */}

    private String getClientId() {

        String clientId = persistentStorage.get(StarlingConstants.StorageKey.CLIENT_ID);

        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalStateException("CLIENT_ID cannot be null or empty!");
        }

        return clientId;
    }

    private String getClientSecret() {

        String clientSecret = persistentStorage.get(StarlingConstants.StorageKey.CLIENT_SECRET);

        if (Strings.isNullOrEmpty(clientSecret)) {
            throw new IllegalStateException("CLIENT_SECRET cannot be null or empty!");
        }

        return clientSecret;
    }

    private String getRedirectUrl() {

        String redirectUrl = persistentStorage.get(StarlingConstants.StorageKey.REDIRECT_URL);

        if (Strings.isNullOrEmpty(redirectUrl)) {
            throw new IllegalStateException("REDIRECT_URL cannot be null or empty!");
        }

        return redirectUrl;
    }

    public String getRefreshToken() throws SessionException {

        OAuth2Token token =
                persistentStorage
                        .get(StarlingConstants.StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (!token.canRefresh()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        return token.getRefreshToken()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not get refresh token, even though canRefresh == true."));
    }
}
