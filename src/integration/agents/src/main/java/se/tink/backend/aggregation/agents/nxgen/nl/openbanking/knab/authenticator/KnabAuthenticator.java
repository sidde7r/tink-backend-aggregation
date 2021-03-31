package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.configuration.KnabConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class KnabAuthenticator implements OAuth2Authenticator {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final KnabApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final KnabConfiguration configuration;

    public KnabAuthenticator(
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            KnabApiClient apiClient,
            PersistentStorage persistentStorage,
            KnabConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    private KnabConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(final String state) {
        String consentId = getScopeConsent().getConsentId();
        persistentStorage.put(StorageKeys.CONSENT_ID, consentId);

        return apiClient.buildAuthorizeUrl(
                state, String.format(QueryValues.CONSENTED_SCOPE, consentId));
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) throws BankServiceException {
        return apiClient.exchangeAuthorizationCode(code, strongAuthenticationState.getState());
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
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    private ConsentResponse getScopeConsent() {

        URL authorizeUrl =
                apiClient.buildAuthorizeUrl(
                        strongAuthenticationState.getState(), QueryValues.INITIAL_SCOPE);

        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl));

        Map<String, String> queryMap =
                this.supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationState.getSupplementalKey(),
                                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                                TimeUnit.MINUTES)
                        .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);

        String codeValue = queryMap.get(KnabConstants.QueryKeys.CODE);
        OAuth2Token oAuth2Token =
                apiClient.exchangeAuthorizationCode(
                        codeValue, strongAuthenticationState.getState());

        return apiClient.getConsent(oAuth2Token);
    }
}
