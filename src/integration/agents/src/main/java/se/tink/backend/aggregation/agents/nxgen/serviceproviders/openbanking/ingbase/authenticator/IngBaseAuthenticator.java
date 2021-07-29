package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator;

import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class IngBaseAuthenticator implements OAuth2Authenticator {

    private static final Logger log = LoggerFactory.getLogger(IngBaseAuthenticator.class);

    private final IngBaseApiClient client;
    private final PersistentStorage persistentStorage;

    public IngBaseAuthenticator(IngBaseApiClient apiClient, PersistentStorage persistentStorage) {
        this.client = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return client.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        final OAuth2Token token = client.getToken(code);
        persistentStorage.put(StorageKeys.AUTHENTICATION_TIME, System.currentTimeMillis());
        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        try {
            final OAuth2Token token = this.client.refreshToken(refreshToken);
            client.setTokenToSession(token);
            return token;
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST
                    && MediaType.APPLICATION_JSON_TYPE.equals(e.getResponse().getType())
                    && e.getResponse().getBody(ErrorResponse.class).isInvalidGrant()) {
                throw SessionError.SESSION_EXPIRED.exception(
                        IngBaseConstants.ErrorMessages.INVALID_GRANT_ERROR);
            } else {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(
                        e.getResponse().getBody(String.class));
            }
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        client.setTokenToSession(accessToken);
    }
}
