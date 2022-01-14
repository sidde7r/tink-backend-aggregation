package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.ConsentResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.ConsentDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class ConsentStatusValidator {

    private final UkOpenBankingApiClient apiClient;
    private final ConsentDataStorage consentDataStorage;

    public ConsentStatusValidator(UkOpenBankingApiClient apiClient, PersistentStorage storage) {
        this.apiClient = apiClient;
        this.consentDataStorage = new ConsentDataStorage(storage);
    }

    public void validate() {
        log.info("[ConsentStatusValidator] Entering consent validator");
        String consentId = consentDataStorage.restoreConsentId();
        if (StringUtils.isEmpty(consentId)) {
            log.info(
                    "[ConsentStatusValidator] ConsentId {} not available in storage. "
                            + "Skipping consent status validation.",
                    consentId);
            return;
        }

        checkIfMarkedWithErrorFlag(consentId);
        checkIfAuthorised(consentId);
        checkIfConsentExpired();
    }

    // TODO: To be removed when consent management becomes stable
    private void checkIfMarkedWithErrorFlag(String consentId) {
        if (consentId.equals(OpenIdAuthenticatorConstants.CONSENT_ERROR_OCCURRED)) {
            SessionKiller.cleanUpAndExpireSession(
                    consentDataStorage.getPersistentStorage(),
                    SessionError.CONSENT_INVALID.exception(
                            "[ConsentStatusValidator] These credentials were marked with "
                                    + "CONSENT_ERROR_OCCURRED flag in the past. Expiring the session."));
        }
    }

    private void checkIfAuthorised(String consentId) {
        try {
            if (isNotAuthorised(consentId)) {
                SessionKiller.cleanUpAndExpireSession(
                        consentDataStorage.getPersistentStorage(),
                        SessionError.CONSENT_INVALID.exception(
                                "[ConsentStatusValidator] Invalid consent status. Expiring the session."));
            }
        } catch (HttpResponseException e) {
            log.warn(
                    "[ConsentStatusValidator] An error has occurred during validation of `{}` consentId",
                    consentId,
                    e);
        }
    }

    private void checkIfConsentExpired() {
        PersistentStorage storage = consentDataStorage.getPersistentStorage();
        storage.get(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_CREATION_DATE, Instant.class)
                .map(creationDate -> creationDate.plus(90, ChronoUnit.DAYS))
                .filter(expirationDate -> Instant.now().isAfter(expirationDate))
                .ifPresent(
                        expirationDate ->
                                SessionKiller.cleanUpAndExpireSession(
                                        storage,
                                        SessionError.CONSENT_INVALID.exception(
                                                "[ConsentStatusValidator] Consent has expired. "
                                                        + "Expiring the session.")));
    }

    private boolean isNotAuthorised(String consentId) {
        log.info("[ConsentStatusValidator] Checking status for consentId {}", consentId);
        ConsentResponseEntity consent =
                apiClient
                        .fetchConsent(consentId)
                        .getData()
                        .orElseThrow(SessionError.CONSENT_REVOKED::exception);
        return consent.isNotAuthorised();
    }
}
