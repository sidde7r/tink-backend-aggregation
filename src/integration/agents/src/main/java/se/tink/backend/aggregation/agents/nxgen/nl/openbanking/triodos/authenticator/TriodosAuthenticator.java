package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants.Oauth2Errors;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.utils.TriodosUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
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
    public URL buildAuthorizeUrl(String state) {
        try {
            URL authorizeUrl = apiClient.getAuthorizeUrl(state);
            // Adding a sleep before returning the authUrl to pause before redirect. Triodos
            // sometimes responds with a "No pending authorisations found" error callback.
            // Assumption is that we redirect too soon after consent resource was created.
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            return authorizeUrl;
        } catch (HttpResponseException e) {
            // Handle that users input incorrect IBAN or IBAN of an account not available through
            // the OB connection. Should be removed when we get rid of the IBAN input [TC-4802]
            ErrorResponse consentError = getErrorBody(e.getResponse());
            throwLoginError(consentError);

            throw e;
        }
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) throws BankServiceException {
        final OAuth2Token token = apiClient.getToken(code);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);

        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {
        final OAuth2Token token = apiClient.refreshToken(refreshToken);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
        consentStatusFetcher.throwSessionErrorIfInvalidConsent();
        return token;
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        String errorType = callbackData.getOrDefault(CallbackParams.ERROR, null);

        throwThirdPartyAppError(callbackData, errorType);
    }

    private void throwThirdPartyAppError(Map<String, String> callbackData, String errorType) {

        String errorDescription =
                callbackData.getOrDefault(CallbackParams.ERROR_DESCRIPTION, "").toLowerCase();

        if (isThirdPartyAppCancelError(errorType, errorDescription)) {
            throw ThirdPartyAppError.CANCELLED.exception();
        }

        if (isThirdPartyAppLoginError(errorType, errorDescription)) {
            log.warn("Callback error: No pending authorizations found");
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }

    private void throwLoginError(ErrorResponse errorResponse) {
        if (errorResponse.isIbanFormatError()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        if (errorResponse.isProductInvalidError()) {
            throw LoginError.NO_ACCOUNTS.exception();
        }
    }

    private ErrorResponse getErrorBody(HttpResponse httpResponse) {
        TriodosUtils.checkErrorResponseBodyType(httpResponse);
        return httpResponse.getBody(ErrorResponse.class);
    }

    private boolean isThirdPartyAppCancelError(String errorType, String errorDescription) {
        return Oauth2Errors.CONSENT_REQUIRED.equalsIgnoreCase(errorType)
                && errorDescription.contains(Oauth2Errors.CANCELLED);
    }

    private boolean isThirdPartyAppLoginError(String errorType, String errorDescription) {
        return Oauth2Errors.INVALID_REQUEST.equalsIgnoreCase(errorType)
                && errorDescription.contains(Oauth2Errors.NO_PENDING_AUTHORIZATIONS);
    }
}
