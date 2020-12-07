package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SwedbankRedirectAuthenticator implements OAuth2Authenticator {
    private final SwedbankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private static final String NOT_IMPLEMENTED = "Not Implemented";

    public SwedbankRedirectAuthenticator(
            SwedbankApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return apiClient.exchangeCodeForToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        OAuth2Token token;
        try {
            token = apiClient.refreshToken(refreshToken);
        } catch (HttpResponseException e) {
            GenericResponse response = e.getResponse().getBody(GenericResponse.class);
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST
                    && response.refreshTokenHasExpired()) {
                throw SessionError.SESSION_EXPIRED.exception(e);
            }
            throw e;
        }
        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, token);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, accessToken);
    }
}
