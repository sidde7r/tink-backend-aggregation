package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccountReferenceEntity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
@RequiredArgsConstructor
public class FinecoStorage {

    private static final String CONSENT_ID = "consentId";
    private static final String CONSENT_CREATION_TIMESTAMP = "TIMESTAMP";
    private static final String TRANSACTIONS_CONSENTS = "transactionAccounts";
    private static final String BALANCES_CONSENTS = "balanceAccounts";
    private static final String PAYMENT_AUTH_URL = "paymentAuthUrl";
    private static final String PAYMENT_AUTH_ID = "paymentAuthId";

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    // Persistent

    public void storeConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public void storeConsentCreationTime(String creationTime) {
        persistentStorage.put(CONSENT_CREATION_TIMESTAMP, creationTime);
    }

    public void storeBalancesConsents(List<AccountReferenceEntity> balancesConsents) {
        persistentStorage.put(BALANCES_CONSENTS, balancesConsents);
    }

    public void storeTransactionsConsents(List<AccountReferenceEntity> transactionsConsents) {
        persistentStorage.put(TRANSACTIONS_CONSENTS, transactionsConsents);
    }

    public String getConsentId() {
        return persistentStorage.get(CONSENT_ID);
    }

    public Optional<LocalDateTime> getConsentCreationTime() {
        return persistentStorage
                .get(CONSENT_CREATION_TIMESTAMP, String.class)
                .map(this::deserializeConsentCreationTime);
    }

    private LocalDateTime deserializeConsentCreationTime(String consentCreationTime) {
        try {
            return LocalDateTime.parse(consentCreationTime);
        } catch (DateTimeParseException e) {
            log.warn("Could not parse consent creation time: " + consentCreationTime);
            // ITE-2404
            // Cleanup old corrupted serialized data from storage that we cannot deserialize.
            persistentStorage.remove(CONSENT_CREATION_TIMESTAMP);
            return null;
        }
    }

    public List<AccountReferenceEntity> getBalancesConsents() {
        return persistentStorage
                .get(BALANCES_CONSENTS, new TypeReference<List<AccountReferenceEntity>>() {})
                .orElse(Collections.emptyList());
    }

    public List<AccountReferenceEntity> getTransactionsConsents() {
        return persistentStorage
                .get(TRANSACTIONS_CONSENTS, new TypeReference<List<AccountReferenceEntity>>() {})
                .orElse(Collections.emptyList());
    }

    // Session

    public void storePaymentAuthorizationUrl(String authUrl) {
        sessionStorage.put(PAYMENT_AUTH_URL, authUrl);
    }

    public String getPaymentAuthorizationUrl() {
        return sessionStorage.get(PAYMENT_AUTH_URL);
    }

    public void storePaymentAuthId(String authId) {
        sessionStorage.put(PAYMENT_AUTH_ID, authId);
    }

    public String getPaymentAuthId() {
        return sessionStorage.get(PAYMENT_AUTH_ID);
    }
}
