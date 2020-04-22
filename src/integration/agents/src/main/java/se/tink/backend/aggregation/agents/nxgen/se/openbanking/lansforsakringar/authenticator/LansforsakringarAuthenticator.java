package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class LansforsakringarAuthenticator implements OAuth2Authenticator {

    private final LansforsakringarApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public LansforsakringarAuthenticator(
            LansforsakringarApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {

        ConsentResponse consent = apiClient.getConsent();
        ConsentResponse authorizeConsentResponse =
                apiClient.authorizeConsent(consent.getConsentId());

        persistentStorage.put(StorageKeys.CONSENT_ID, authorizeConsentResponse.getConsentId());
        persistentStorage.put(
                StorageKeys.AUTHORIZATION_ID, authorizeConsentResponse.getAuthorisationId());

        return apiClient.buildAuthorizeUrl(state, authorizeConsentResponse.getAuthorisationId());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return apiClient.exchangeAuthorizationCode(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws BankServiceException, SessionException {
        try {
            final OAuth2Token accessToken = apiClient.refreshToken(refreshToken);
            sessionStorage.put(LansforsakringarConstants.StorageKeys.ACCESS_TOKEN, accessToken);
            return accessToken;
        } catch (HttpResponseException e) {
            ErrorResponse response = e.getResponse().getBody(ErrorResponse.class);
            if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN
                    && response.isAnyServiceBlocked()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            throw e;
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(LansforsakringarConstants.StorageKeys.ACCESS_TOKEN, accessToken);
    }
}
