package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.entities.DataResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.AccountSetupResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.ClientCredentialTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.ErrorBody;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class ICSOAuthAuthenticator implements OAuth2Authenticator {

    private final ICSApiClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public ICSOAuthAuthenticator(
            ICSApiClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        final DataResponseEntity accountRequestData =
                Optional.ofNullable(client.fetchTokenWithClientCredential())
                        .map(ClientCredentialTokenResponse::toTinkToken)
                        .map(client::setupAccount)
                        .filter(AccountSetupResponse::receivedAllReadPermissions)
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.MISSING_PERMISSIONS))
                        .getData();

        storeTransactionFromDate(accountRequestData.getTransactionFromDate());
        sessionStorage.put(StorageKeys.STATE, state);
        // refresh expiry date is sent in the account request data, not in the token
        sessionStorage.put(StorageKeys.EXPIRATION_DATE, accountRequestData.getExpirationDate());
        return client.createAuthorizeRequest(state, accountRequestData.getAccountRequestId())
                .getUrl();
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return addRefreshExpireToToken(client.fetchToken(code));
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        try {
            return client.refreshToken(refreshToken);
        } catch (HttpResponseException e) {
            ErrorBody errorBody = e.getResponse().getBody(ErrorBody.class);
            if ((e.getResponse().getStatus() == ICSConstants.ErrorCode.UNAUTHORIZED
                            && ICSConstants.ErrorMessages.INVALID_TOKEN.equalsIgnoreCase(
                                    errorBody.getError()))
                    || (e.getResponse().getStatus() == ICSConstants.ErrorCode.FORBIDDEN
                            && ICSConstants.ErrorMessages.CONSENT_ERROR.equalsIgnoreCase(
                                    errorBody.getError()))) {
                throw new SessionException(SessionError.SESSION_EXPIRED);
            }
            throw e;
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        this.client.setToken(accessToken);
    }

    private void storeTransactionFromDate(Date date) {
        persistentStorage.put(StorageKeys.TRANSACTION_FROM_DATE, date);
    }

    private OAuth2Token addRefreshExpireToToken(OAuth2Token token) {
        if (!token.hasRefreshExpire()) {
            sessionStorage
                    .get(StorageKeys.EXPIRATION_DATE, Date.class)
                    .ifPresent(
                            expirationDate -> {
                                final long millisToExpire =
                                        expirationDate.getTime() - new Date().getTime();
                                token.setRefreshExpiresInSeconds(millisToExpire / 1000);
                            });
        }
        return token;
    }
}
