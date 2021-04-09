package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ConsentStatusValidator {

    private final UkOpenBankingApiClient apiClient;
    private final PersistentStorage storage;

    public ConsentStatusValidator(UkOpenBankingApiClient apiClient, PersistentStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
    }

    public void validate() {
        String consentId =
                storage.get(
                                UkOpenBankingV31Constants.PersistentStorageKeys
                                        .AIS_ACCOUNT_CONSENT_ID,
                                String.class)
                        .orElse(StringUtils.EMPTY);

        // To be removed when consent management becomes stable
        if (consentId.equals(OpenIdAuthenticatorConstants.CONSENT_ERROR_OCCURRED)) {
            cleanUpAndExpireSession(
                    consentId,
                    "These credentials were marked with CONSENT_ERROR_OCCURRED flag in the past. Expiring the session.");
        }

        if (StringUtils.isNotEmpty(consentId) && isNotAuthorised(consentId)) {
            cleanUpAndExpireSession(consentId, "Invalid consent status. Expiring the session.");
        }
    }

    private boolean isNotAuthorised(String consentId) {

        // PLACEHOLDER: Add permissions vs refresh items validation

        return apiClient.fetchConsent(consentId).getData().isNotAuthorised();
    }

    private void cleanUpAndExpireSession(String consentId, String errorMsg) {
        apiClient.deleteConsent(consentId);

        storage.remove(UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID);
        storage.remove(UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN);

        throw SessionError.CONSENT_INVALID.exception(errorMsg);
    }
}
