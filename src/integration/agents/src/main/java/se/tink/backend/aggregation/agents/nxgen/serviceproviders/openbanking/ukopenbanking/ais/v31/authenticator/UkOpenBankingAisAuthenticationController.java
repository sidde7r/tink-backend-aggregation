package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class UkOpenBankingAisAuthenticationController extends OpenIdAuthenticationController {

    private final ConsentStatusValidator consentStatusValidator;

    public UkOpenBankingAisAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            UkOpenBankingApiClient apiClient,
            OpenIdAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            String callbackUri,
            RandomValueGenerator randomValueGenerator,
            OpenIdAuthenticationValidator authenticationValidator,
            ConsentStatusValidator consentStatusValidator) {
        super(
                persistentStorage,
                supplementalInformationHelper,
                apiClient,
                authenticator,
                credentials,
                strongAuthenticationState,
                callbackUri,
                randomValueGenerator,
                authenticationValidator);

        this.consentStatusValidator = consentStatusValidator;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        super.autoAuthenticate();
        validateConsentStatus();
    }

    public void validateConsentStatus() {
        String consentId =
                persistentStorage
                        .get(
                                UkOpenBankingV31Constants.PersistentStorageKeys
                                        .AIS_ACCOUNT_CONSENT_ID,
                                String.class)
                        .orElse(StringUtils.EMPTY);

        // To be removed when consent management becomes stable
        if (consentId.equals(OpenIdAuthenticatorConstants.CONSENT_ERROR_OCCURRED)) {
            cleanUpAndExpireSession(
                    "These credentials were marked with CONSENT_ERROR_OCCURRED flag in the past. Expiring the session.");
        }

        if (StringUtils.isNotEmpty(consentId)
                && consentStatusValidator.isInvalidWithRetry(consentId, 2)) {
            cleanUpAndExpireSession("Invalid consent status. Expiring the session.");
        }
    }

    private void cleanUpAndExpireSession(String errorMsg) {

        // PLACEHOLDER: Delete invalid consent

        persistentStorage.remove(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID);
        persistentStorage.remove(UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN);

        throw SessionError.CONSENT_INVALID.exception(errorMsg);
    }
}
