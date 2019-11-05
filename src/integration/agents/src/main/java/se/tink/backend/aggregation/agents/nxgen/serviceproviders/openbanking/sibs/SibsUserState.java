package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.SibsSignSteps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.Consent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SibsUserState {

    private static final String CONSENT_ID = "CONSENT_ID";
    private static final String SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS =
            "sibs_manual_authentication_in_progress";
    private static final int DAYS_BACK_TO_FETCH_TRANSACTIONS = 10;
    private static final String PAGINATION_DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(PAGINATION_DATE_FORMAT);
    private static final LocalDate TRANSACTIONS_FROM_BEGINNING = LocalDate.of(1970, 1, 1);

    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    SibsUserState(final PersistentStorage persistentStorage, final Credentials credentials) {
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    public String getConsentId() {
        Consent consent =
                persistentStorage
                        .get(CONSENT_ID, Consent.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SessionError.SESSION_EXPIRED.exception()));
        return consent.getConsentId();
    }

    public void removeConsent() {
        persistentStorage.remove(CONSENT_ID);
    }

    public void startManualAuthentication(final ConsentResponse consentResponse) {
        Consent consent =
                new Consent(consentResponse.getConsentId(), LocalDateTime.now().toString());
        persistentStorage.put(SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS, true);
        persistentStorage.put(CONSENT_ID, consent);
    }

    public boolean isManualAuthenticationInProgress() {
        return persistentStorage
                .get(SibsSignSteps.SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS, Boolean.class)
                .orElse(false);
    }

    public void finishManualAuthentication() {
        persistentStorage.put(SibsSignSteps.SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS, false);
    }

    public String getTransactionsFetchBeginDate(final Account account) {
        LocalDate lastDate =
                Optional.ofNullable(credentials.getUpdated())
                        .map(d -> new java.sql.Date(d.getTime()).toLocalDate())
                        .orElse(TRANSACTIONS_FROM_BEGINNING);
        if (lastDate.getYear() != TRANSACTIONS_FROM_BEGINNING.getYear()) {
            lastDate = lastDate.minusDays(DAYS_BACK_TO_FETCH_TRANSACTIONS);
        }
        return DATE_FORMATTER.format(lastDate);
    }
}
