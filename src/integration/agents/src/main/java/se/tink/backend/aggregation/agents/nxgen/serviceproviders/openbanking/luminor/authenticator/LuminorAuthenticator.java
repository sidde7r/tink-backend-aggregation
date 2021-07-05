package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class LuminorAuthenticator implements OAuth2Authenticator {

    private final LuminorApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;

    public LuminorAuthenticator(
            LuminorApiClient apiClient,
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {
        final OAuth2Token token = apiClient.createToken(code);
        persistentStorage.put(LuminorConstants.StorageKeys.OAUTH_TOKEN, token);
        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        final OAuth2Token token = apiClient.refreshToken(refreshToken);
        persistentStorage.put(LuminorConstants.StorageKeys.OAUTH_TOKEN, token);

        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        String storedConsentId = persistentStorage.get(LuminorConstants.HeaderKeys.CONSENT_ID);
        if (!apiClient.isConsentValid(storedConsentId)) {
            AccountsResponse accountsResponse = apiClient.getAccountList();
            List<AccountEntity> accounts = accountsResponse.getAccounts();
            List<String> ibans =
                    accounts.stream().map(AccountEntity::getIban).collect(Collectors.toList());

            ConsentResponse consentResponse =
                    apiClient.createConsentResource(ibans, strongAuthenticationState.getState());
            String consentId = consentResponse.getConsentId();
            persistentStorage.put(LuminorConstants.HeaderKeys.CONSENT_ID, consentId);

            handleScaRedirect(consentResponse);

            if (!apiClient.isConsentValid(consentResponse.getConsentId())) {
                // throw AuthError;
            }
        }
        persistentStorage.put(LuminorConstants.StorageKeys.OAUTH_TOKEN, accessToken);
    }

    public void handleScaRedirect(ConsentResponse consentResponse) {
        URL url = new URL(consentResponse.getLinks().getScaRedirect().getHref());
        String consentId = consentResponse.getConsentId();
        ConsentStatusResponse consentStatusResponse = apiClient.getConsentStatus(consentId);
        // add error handling
        if (consentStatusResponse.getConsentStatus().equalsIgnoreCase("received")) {
            supplementalInformationHelper.openThirdPartyApp(
                    ThirdPartyAppAuthenticationPayload.of(url));

            Map<String, String> queryMap =
                    this.supplementalInformationHelper
                            .waitForSupplementalInformation(
                                    strongAuthenticationState.getSupplementalKey(),
                                    ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                                    TimeUnit.MINUTES)
                            .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);

            String codeValue = queryMap.get(CallbackParams.CODE);
            if ("ok".equalsIgnoreCase(codeValue)) {
                return;
            } // add auth error if nok
            // Illegal state exc
        }
    }
}
