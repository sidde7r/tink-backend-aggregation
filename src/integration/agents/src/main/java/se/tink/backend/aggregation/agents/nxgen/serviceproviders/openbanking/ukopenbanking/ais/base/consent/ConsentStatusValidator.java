package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.AccountAccessConsentsDataResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
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
        log.info("[CONSENT STATUS VALIDATOR] Entering consent validator");
        String consentId = restoreConsentId();
        if (StringUtils.isEmpty(consentId)) {
            log.info(
                    "[CONSENT STATUS VALIDATOR] ConsentId {} not available in storage. Skipping consent status validation.",
                    consentId);
            return;
        }

        checkIfMarkedWithErrorFlag(consentId);
        checkIfAuthorised(consentId);
        checkIfConsentExpired();
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
        try {
            if (isNotAuthorised(consentId)) {
                SessionKiller.cleanUpAndExpireSession(
                        storage,
                        SessionError.CONSENT_INVALID.exception(
                                "Invalid consent status. Expiring the session."));
            }
        } catch (HttpResponseException e) {
            log.warn(
                    "[CONSENT STATUS VALIDATOR] An error has occurred during validation of `{}` consentId",
                    consentId,
                    e);
        }
    }

    private void checkIfConsentExpired() {
        storage.get(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_CREATION_DATE, Instant.class)
                .map(creationDate -> creationDate.plus(90, ChronoUnit.DAYS))
                .filter(expirationDate -> Instant.now().isAfter(expirationDate))
                .ifPresent(
                        expirationDate ->
                                SessionKiller.cleanUpAndExpireSession(
                                        storage,
                                        SessionError.CONSENT_INVALID.exception(
                                                "Consent has expired. Expiring the session.")));
    }

    private boolean isNotAuthorised(String consentId) {
        log.info("[CONSENT STATUS VALIDATOR] Checking status for consentId {}", consentId);
        AccountAccessConsentsDataResponseEntity consent =
                apiClient
                        .fetchConsent(consentId)
                        .getData()
                        .orElseThrow(SessionError.CONSENT_REVOKED::exception);
        return consent.isNotAuthorised();
    }
}
