package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator;

import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.Oauth2Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class TriodosAuthenticator extends BerlinGroupAuthenticator {

    private final TriodosApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final ConsentStatusFetcher consentStatusFetcher;

    public TriodosAuthenticator(
            final TriodosApiClient apiClient,
            PersistentStorage persistentStorage,
            ConsentStatusFetcher consentStatusFetcher) {
        super(apiClient);
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.consentStatusFetcher = consentStatusFetcher;
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) throws BankServiceException {
        final OAuth2Token token = apiClient.getToken(code);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);

        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {
        if (!consentStatusFetcher.isConsentValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        final OAuth2Token token = apiClient.refreshToken(refreshToken);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);

        return token;
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        String errorType = callbackData.getOrDefault(CallbackParams.ERROR, null);
        String errorDescription =
                callbackData.getOrDefault(CallbackParams.ERROR_DESCRIPTION, "").toLowerCase();

        if (Oauth2Errors.CONSENT_REQUIRED.equalsIgnoreCase(errorType)
                && errorDescription.contains(Oauth2Errors.CANCELLED)) {
            throw ThirdPartyAppError.CANCELLED.exception();
        }
    }
}
