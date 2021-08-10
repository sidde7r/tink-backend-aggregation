package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

/**
 * Knab authentication flow require two redirects if user doesn't have an active consent:
 *
 * <p>Step 1: PSU authentication - User approves psd2 and offline access.
 *
 * <p>Step 2: Authorization of consent - With the access token we got from step 1 we make a request
 * to create a consent resource. For a successful request we get a consent ID back. With this
 * consent ID we initiate yet another redirect with the consent ID in the scope. This time the user
 * approves the consent for fetching information about their accounts.
 */
@RequiredArgsConstructor
public class KnabAuthenticator implements OAuth2Authenticator {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final KnabApiClient apiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public URL buildAuthorizeUrl(final String state) {
        return apiClient.buildAuthorizeUrl(state, KnabConstants.Scopes.PSU_AUTHENTICATION);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) throws BankServiceException {
        TokenResponse tokenResponse =
                apiClient.exchangeAuthorizationCode(code, strongAuthenticationState.getState());

        // PSD2 consent is required for making any request to the Knab API
        if (!tokenResponse.userHasAuthorizedPsd2Consent()) {
            throw SessionError.CONSENT_INVALID.exception();
        }

        return tokenResponse.toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        final OAuth2Token accessToken = apiClient.refreshToken(refreshToken);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
        return accessToken;
    }

    @Override
    public void useAccessToken(final OAuth2Token accessToken) {
        // No need to create a new consent if the current one is still valid
        if (!apiClient.isConsentValid(persistentStorage.get(StorageKeys.CONSENT_ID), accessToken)) {
            triggerAuthorizeConsentFlow(accessToken);
        } else {
            persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
        }
    }

    private void triggerAuthorizeConsentFlow(final OAuth2Token accessToken) {

        persistentStorage.put(
                StorageKeys.CONSENT_ID, apiClient.getConsent(accessToken).getConsentId());

        URL authorizeUrl =
                apiClient.buildAuthorizeUrl(
                        strongAuthenticationState.getState(),
                        String.format(
                                KnabConstants.Scopes.AUTHORIZE_CONSENT,
                                persistentStorage.get(StorageKeys.CONSENT_ID)));

        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl));

        Map<String, String> queryMap =
                this.supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationState.getSupplementalKey(),
                                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                                TimeUnit.MINUTES)
                        .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);

        String codeValue = queryMap.get(OAuth2Constants.CallbackParams.CODE);
        TokenResponse tokenResponse =
                apiClient.exchangeAuthorizationCode(
                        codeValue, strongAuthenticationState.getState());

        persistentStorage.put(StorageKeys.OAUTH_TOKEN, tokenResponse.toTinkToken());
    }
}
