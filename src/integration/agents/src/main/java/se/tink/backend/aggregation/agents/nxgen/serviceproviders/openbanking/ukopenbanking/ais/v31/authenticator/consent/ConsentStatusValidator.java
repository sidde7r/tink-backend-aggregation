package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.SessionKiller;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class ConsentStatusValidator {

    private final UkOpenBankingApiClient apiClient;
    private final PersistentStorage storage;

    public ConsentStatusValidator(UkOpenBankingApiClient apiClient, PersistentStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
    }

    public void validate() {
        String consentId = restoreConsentId();
        if (StringUtils.isEmpty(consentId)) {
            log.info(
                    "[CONSENT STATUS VALIDATOR] ConsentId {} not available in storage. Skipping consent status validation.",
                    consentId);
            return;
        }

        checkIfMarkedWithErrorFlag(consentId);
        checkIfAuthorised(consentId);
    }

    private String restoreConsentId() {
        return storage.get(
                        UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID,
                        String.class)
                .orElse(StringUtils.EMPTY);
    }

    // TODO: To be removed when consent management becomes stable
    private void checkIfMarkedWithErrorFlag(String consentId) {
        if (consentId.equals(OpenIdAuthenticatorConstants.CONSENT_ERROR_OCCURRED)) {
            SessionKiller.cleanUpAndExpireSession(
                    storage,
                    SessionError.CONSENT_INVALID.exception(
                            "These credentials were marked with CONSENT_ERROR_OCCURRED flag in the past. Expiring the session."));
        }
    }

    private void checkIfAuthorised(String consentId) {
        if (isNotAuthorised(consentId)) {
            SessionKiller.cleanUpAndExpireSession(
                    storage,
                    SessionError.CONSENT_INVALID.exception(
                            "Invalid consent status. Expiring the session."));
        }
    }

    private boolean isNotAuthorised(String consentId) {
        log.info("[CONSENT STATUS VALIDATOR] Checking consent status for consentId {}", consentId);
        return apiClient.fetchConsent(consentId).getData().isNotAuthorised();
    }
}
